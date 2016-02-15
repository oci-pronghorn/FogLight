package com.ociweb.device.grove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2I2CStage extends PronghornStage {

    private static final Logger logger = LoggerFactory.getLogger(GroveShieldV2I2CStage.class);
    
    private int taskAtHand;
    private int stepAtHand;
    
    public int cyclesToWait;
    public int byteToSend;
    private int byteToSendPos;
    
    //holds the same array as used by the Blob from the ring.
    private byte[] bytesToSendBacking; //set before send
    private int    bytesToSendRemaining;
    private int    bytesToSendPosition;
    private int    bytesToSendMask;
    private int    bytesToSendReleaseSize;
            
    
    private int bitFromBus;
    
    private static final int TASK_NONE = 0;
    private static final int TASK_MASTER_START = 1;
    private static final int TASK_MASTER_STOP  = 2;
    private static final int TASK_WRITE_BYTES  = 3;
    
    public final GroveConnectionConfiguration config;
    
    private final Pipe<I2CCommandSchema> request;
    private final Pipe<I2CCommandSchema> response;
  
    private long cycleTops = 0;
    private long startTime = 0;
    private long duration = 0;
    
    //I2C is a little complex to ensure correctness.  As a result this stage is not aware of any 
    //specific grove modules which may be attached. It only does the sending and receiving of bytes
    
//    public GroveShieldV2I2CStage(GraphManager gm, Pipe<GroveI2CSchema> request, Pipe<GroveI2CSchema> response, GroveConnectionConfiguration config) {
//        super(gm, request, response);
//        
//        this.request = request;
//        this.response = response;
//        this.config = config;
//        
//        //Fixed at a slow 100K per second for broad compatibility
//        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 100*1000, this);
//        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
//        
//    }
    
    //must be between 10*1000 and 100*1000 for SMBus and I2C, NOTE some signals use two+ cycles so 10_000 is far too small.
    //NOTE: this is the tick so the target cycle is half this speed
    public static final int NS_PAUSE = (1_000_000_000)/(800_000); 
    
    public GroveShieldV2I2CStage(GraphManager gm, Pipe<I2CCommandSchema> request, GroveConnectionConfiguration config) {
        super(gm, request, NONE);
        
        this.request = request;
        this.response = null;
        this.config = config;
        
        //NOTE: this assumes the scheduler will never get aggressive an will always respect the call rate
        //      even when a call to run is longer than the requested period.
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, NS_PAUSE, this);
        
        
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
        
    }
    
    //only used in startup
    private void pause() {
        try {
          //  Thread.sleep(NS_PAUSE/1_000_000,NS_PAUSE%1_000_000);
            Thread.sleep(40); //timeout for SMBus
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void startup() {
        //bus read/write priority should be the lowest an need not conflict with other tasks.
        //if shared with another thread wanting to be high leave it alone
        if (Thread.currentThread().getPriority()!=Thread.MAX_PRIORITY) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        }
        
        if (config.configI2C) {
           config.beginPinConfiguration();
           config.configurePinsForI2C();
           config.endPinConfiguration();
           config.i2cClockOut();
           config.i2cDataOut();
           int i;
           i = 1100;
           while (--i>=0) { //force hot spot to optimize this call
               config.i2cSetClockHigh();
               config.i2cReadClockBool();
           }
           pause();
           i = 1100;
           while (--i>=0) { //force hot spot to optimize this call
               config.i2cSetDataHigh();
              // config.i2cReadAck();
           }
           pause();
           
        } else {
            System.out.println("warning, i2s stage used but not turned on");
        }
        //starting in the known state where both are high

          if (0==config.i2cReadClock() ) {
              throw new RuntimeException("expected clock to be high for start");
          }        
          if (0==config.i2cReadData() ) {
              throw new RuntimeException("expected data to be high for start");
          }

      config.lastTime = startTime = System.nanoTime();
    }

    @Override
    public void run() {

        if (TASK_NONE == taskAtHand) {
            readRequest();//may populate task at hand
        }
        
        //required wait based on slave device needs
        if (cyclesToWait > 0) {
            --cyclesToWait;
            return;
        }
        
        if (TASK_WRITE_BYTES == taskAtHand) {
            writeBytes();            
        } else if (TASK_MASTER_START == taskAtHand) {
            masterStart();            
        } else {
            masterStop();            
        }
        
        //Must return after this point to ensure the clock speed is respected.
        config.progressLog(taskAtHand, stepAtHand, byteToSend);

    }
    
    long lastTimeX;    

    //TODO: try dropping file and recreating to read.

    
    private void readRequest() {
        
        if (Pipe.hasContentToRead(request)) {
            
            int msgId = Pipe.takeMsgIdx(request);
            if (msgId < 0) {
                requestShutdown();
                return;
            }
            bytesToSendReleaseSize =  Pipe.sizeOf(request, msgId);
            
            switch(msgId) {
                case I2CCommandSchema.MSG_COMMAND_1:
                    
                    
                    int meta = Pipe.takeRingByteMetaData(request);
                    int len = Pipe.takeRingByteLen(request);
                    if (len>0) {
                        
                      //will mess up time, do not use  
                      //  System.out.println("                        READING FROM QUEUE NEW COMMAND");
                        
                        bytesToSendBacking = Pipe.byteBackingArray(meta, request);
                        bytesToSendMask = Pipe.blobMask(request);
                        bytesToSendPosition = Pipe.bytePosition(meta, request, len);
                        bytesToSendRemaining = len;
                        
                        taskAtHand = TASK_MASTER_START;
                        stepAtHand = 0;
                        
                        byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
                        byteToSendPos = 8;
                    }
                break;
                case I2CCommandSchema.MSG_SETDELAY_10:  
                    
                    //will mess up time, do not use 
                    System.out.println("                        READING FROM QUEUE DELAY");
                    
                    int offset = Pipe.takeValue(request); //TODO: ignrore?? old feature we may not want.
                    
                    cyclesToWait = 1 + (Pipe.takeValue(request)/NS_PAUSE); //schedule rate so value is in NS
                    Pipe.confirmLowLevelRead(request,bytesToSendReleaseSize);
                    Pipe.releaseReadLock(request);
                break;    
                default:
                    requestShutdown();
                    return;
                   
            }
        }

    }


    private void masterStart() {
     
        switch (stepAtHand) {
            case 0:
                config.i2cSetClockHigh();
                cycleTops++;

                stepAtHand = 2;
                break;
            case 2:
                if (config.i2cReadClockBool()) {
                    if (0!=config.i2cReadData()) { //TODO: readACK????
                        config.i2cSetDataLow(); //lower data while clock is high
                        stepAtHand = 4;
                    } else {
                        logger.trace("unable to be master, data line should be high, will try again.");
        
                        config.i2cSetDataHigh(); //force the issue.
                        taskAtHand = TASK_MASTER_START;
                        stepAtHand = 0;//so try again.
                        return;
                    }
                } else {
                    logger.trace("clock stretching now.");
                    taskAtHand = TASK_MASTER_START;
                    stepAtHand = 0;//so try again.
                    return;//clock stretching, will come back to this state next cycle around                     
                }
                break;
            case 4:
                lastBit = 0;//data is set low and may be helpful for first bit
                config.i2cSetClockLow();
                stepAtHand = 0;
                taskAtHand = TASK_WRITE_BYTES;
                break;//done
            default:
                throw new UnsupportedOperationException();
        }

    }

    private void masterStop() {
   
        switch (stepAtHand) {
            case 0:
                config.i2cSetDataLow();
                stepAtHand = 1;
                break;
            case 1:
                config.i2cSetClockHigh();
                cycleTops++;
                stepAtHand = 3;
                break;
            case 3:
                if (config.i2cReadClockBool()) {
                    config.i2cSetClockHigh();
                    cycleTops++;
                    stepAtHand = 5;
                } else {
                    logger.trace("clock stretching now.");
                    stepAtHand = 4;
                    return;//clock stretching, will come back to this state next cycle around                     
                }
                break;
            case 4: //clock stretching
                if (config.i2cReadClockBool()) {
                    config.i2cSetClockHigh();
                    cycleTops++;
                    stepAtHand = 5;
                } else {
                    logger.trace("clock stretching now.");
                    stepAtHand = 4;
                    return;//clock stretching, will come back to this state next cycle around                     
                }
                break;
            case 5: //done
                config.i2cSetDataHigh();
                stepAtHand = 0;
                taskAtHand = TASK_NONE;
                break; 
        }        
    }

    //TODO: add multiple pipes in
    //TODO: add mulitple pipes out with array to map addr to the pipe for responses
    //TODO: file io set postion when done.
    //TODO: produce one stage per grove component with its own schema
    
    
    int lastBit = -1;
    
    private void writeBytes() {

        switch (stepAtHand) {
            case 0:
                  //byteToSendPos starts with 8
                  if (0==(1 & (byteToSend >> (--byteToSendPos)))) {
                      //System.out.println("0 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      if (0!=lastBit) {
                        config.i2cSetDataLow();
                        lastBit = 0;
                        stepAtHand = 1;
                        break;
                      }
                  } else {
                      //System.out.println("1 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      if (1!=lastBit) {
                        config.i2cSetDataHigh();
                        lastBit = 1;
                        stepAtHand = 1;
                        break;
                      }
                  } 
                  //fall through if the value is already what we need.
            case 1:
                  config.i2cSetClockHigh();
                  cycleTops++;
                  stepAtHand = 2;
                  break;
            case 2:
                  if (config.i2cReadClockBool()) {
                      config.i2cSetClockLow();
                      //jump to 4 when byteToEndPos is zero else jump to 0 
                      stepAtHand = 4 & ((byteToSendPos-1)>>31);
                  } else {
                      logger.trace("clock stretching now.");
                      return;//clock stretching, will come back to this state next cycle around                     
                  }
                  break;
            case 4:
                config.i2cSetDataHigh(); //set high so the ack can make this low         
                stepAtHand = 5;
                break;
            case 5:                 
                config.i2cSetClockHigh();
                cycleTops++;
                stepAtHand = 7;
                break;
            case 7:
                if (config.i2cReadClockBool()) {
                    config.i2cSetClockLow(); 
                    stepAtHand = 9;
                } else {
                    logger.trace("clock stretching now.");
                    return;//clock stretching, will come back to this state next cycle around                     
                }
                break;
            case 9:

//                boolean debug = false; //may slow down response causing device to timeout, do not use
//                if (debug) {
//                    System.out.print(" sent 0x"+Integer.toHexString(byteToSend)+"  LEFT(1)"+Integer.toHexString(byteToSend>>1)+"  ");
//                }  
                
                boolean ack = config.i2cReadAck();

                lastBit = -1;
                
                if (!ack) {
                    //What do we do up on ack?
                    //roll back and try again?
      //              System.out.println("failure on send 0x"+Integer.toHexString(byteToSend)+"  LEFT(1)"+Integer.toHexString(byteToSend>>1)+"  ");
        //            System.out.println();
                } else {
                    //TODO: need to add flag that some devices respond with ack and others do not
                   // System.out.println("ok  "+Integer.toHexString(byteToSend));                    
                }
                
             
                stepAtHand = 0;
                
                if (--bytesToSendRemaining<=0) {
                    taskAtHand = TASK_MASTER_STOP; //we are all done
                    
                    //release the resources from the pipe for more data
                    Pipe.confirmLowLevelRead(request,bytesToSendReleaseSize);
                    Pipe.releaseReadLock(request);
                    
                    
                } else {       
                    
                    byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
                    byteToSendPos = 8;
                    //System.out.println("sending byte "+Integer.toHexString(byteToSend));
                    //now back to zero to send the next byte 
                }
                break;//pause
      
        }
    }

    private void readBit() {
        switch (stepAtHand) {
            case 0:
                  //wait for clock to be highwriteBytes
                  while (0==config.i2cReadClock() ) {
                    //This is a spinning block dependent upon the other end of i2c
                  }//writeValue
                  //clock is now high
                  bitFromBus = config.i2cReadData();                    
                  stepAtHand = 1;
                  break;//pause
            case 1:
                  config.i2cSetClockLow();
                
                  stepAtHand = 0;
                  
                  taskAtHand = 0;
                  
                  break;//pause
            default:
                throw new UnsupportedOperationException();        
        }
        
    }
    

    @Override
    public void shutdown() {
        duration = System.nanoTime()-startTime;
        
        long avgPeriod = duration/cycleTops;
        long hz = 1_000_000_000/avgPeriod;
        
        logger.warn("Cycles per second {} ",hz);
        
    }

   
    
}
