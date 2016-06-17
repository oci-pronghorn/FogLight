package com.ociweb.iot.grove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.schema.I2CCommandSchema;
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
    private static final int TASK_WRITE_BYTES  = 3;
    private static final int TASK_MASTER_STOP  = 5;
    private static final int TASK_DELAY        = 7;


    public final Hardware config;
    
    private final Pipe<I2CCommandSchema>[] request;
    private final Pipe<I2CCommandSchema> response;

    private Pipe<I2CCommandSchema> activePipe;

    private long cycleTops = 0;
    private long startTime = 0;
    private long duration = 0;
    
    private int pipeIdx;

    private int lastBit = -1;

    //I2C is a little complex to ensure correctness.  As a result this stage is not aware of any
    //specific grove modules which may be attached. It only does the sending and receiving of bytes

    
    //must be between 10*1000 and 100*1000 for SMBus and I2C, NOTE some signals use two+ cycles so 10_000 is far too small.
    //NOTE: this is the tick so the target cycle is half this speed
    //NOTE: these devices are I2C and do not show any lower bounds for speed.
    public static final int NS_PAUSE = (1_000_000_000)/(800_000); //slower cycles is less power consumption.
    
    public GroveShieldV2I2CStage(GraphManager gm, Pipe<I2CCommandSchema>[] request, Pipe[] response, Hardware config) {
        super(gm, request, response);
        
        this.request = request;
        this.response = null;
        this.config = config;
        
        this.pipeIdx = request.length;


        //NOTE: this assumes the scheduler will never get aggressive an will always respect the call rate
        //      even when a call to run is longer than the requested period.
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, NS_PAUSE, this);
        
        
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
        
    }
    
    //only used in startup
    private void pause() {
        try {
          //  Thread.sleep(NS_PAUSE/1_000_000,NS_PAUSE%1_000_000);
            Thread.sleep(35); //timeout for SMBus
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void startup() {

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        
        if (config.configI2C) {
           config.beginPinConfiguration();
           config.configurePinsForI2C();
           config.endPinConfiguration();
           config.i2cClockOut();
           config.i2cDataOut();
           boolean temp = false;
           int i;
           i = 1100;
           while (--i>=0) { //force hot spot to optimize this call
               temp |= config.i2cReadClockBool();
               config.i2cSetClockHigh();//done last
           }
           pause();
           i = 1100;
           while (--i>=0) { //force hot spot to optimize this call
               temp |= config.i2cReadAck();
               temp |= config.i2cReadDataBool();
               config.i2cSetDataHigh();//done last
           }
           if (!temp) {
               throw new UnsupportedOperationException();
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
      startTime = System.nanoTime();
    }

    @Override
    public void run() {

        //These are ordered by the most frequent first and the least last
        int localHand = taskAtHand;
        if (TASK_WRITE_BYTES == localHand) {

            writeBytes();

        } else if (TASK_MASTER_START == localHand) {

            masterStart();

        } else if (TASK_MASTER_STOP == localHand) {

            masterStop();

        } else if (TASK_NONE == localHand) {

            readRequest();

        } else if (TASK_DELAY == localHand) {

            delay();//this is for slowing down so putting it very last is good

        }
        
        //Must return after this point to ensure the clock speed is respected.
        config.progressLog(taskAtHand, stepAtHand, byteToSend);

    }

    private void delay() {
        //required wait based on slave device needs
        if (cyclesToWait > 0) {
            --cyclesToWait;
        } else {
            taskAtHand = TASK_NONE;
            readRequest();

        }
    }

    long lastTimeX;
    
    private void readRequest() {

        activePipe = selectPipe();

        if (null != activePipe && Pipe.hasContentToRead(activePipe)) {
            
            int msgId = Pipe.takeMsgIdx(activePipe);
            if (msgId < 0) {
                requestShutdown();
                return;
            }

            bytesToSendReleaseSize =  Pipe.sizeOf(activePipe, msgId);
            
            switch(msgId) {
                case I2CCommandSchema.MSG_COMMAND_1:
                    readCommandMessage();
                break;
                case I2CCommandSchema.MSG_SETDELAY_10:
                    readDelayMessage();
                break;
            }
        }

    }



    private Pipe<I2CCommandSchema> selectPipe() {

     //   return request[0];
        if (0==pipeIdx) {
            pipeIdx = request.length;
        }
        int startIdx = pipeIdx;
        do {
            if (Pipe.contentRemaining(request[--pipeIdx]) > 0) {
                return request[pipeIdx];
            }
            if (0==pipeIdx) {
                pipeIdx = request.length;
            }
        } while (pipeIdx != startIdx);
        return null;

    }

    private void readDelayMessage() {
        //will mess up time, do not use
        System.out.println("                        READING FROM QUEUE DELAY");

        int offset = Pipe.takeValue(activePipe); //TODO: ignrore?? old feature we may not want.

        cyclesToWait = 1 + (Pipe.takeValue(activePipe)/NS_PAUSE); //schedule rate so value is in NS

        //we do not need the data so release now.
        Pipe.confirmLowLevelRead(activePipe,bytesToSendReleaseSize);
        Pipe.releaseReadLock(activePipe);

        taskAtHand = TASK_DELAY;
    }

    private void readCommandMessage() {
        int meta = Pipe.takeRingByteMetaData(activePipe);
        int len = Pipe.takeRingByteLen(activePipe);
        if (len>0) {

          //will mess up time, do not use
           // System.out.println("                        READING FROM QUEUE NEW COMMAND");

            bytesToSendBacking = Pipe.byteBackingArray(meta, activePipe);
            bytesToSendMask = Pipe.blobMask(activePipe);
            bytesToSendPosition = Pipe.bytePosition(meta, activePipe, len);
            bytesToSendRemaining = len;

            taskAtHand = TASK_MASTER_START;
            stepAtHand = 0;

            byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
            byteToSendPos = 8;
        }

        //NOTE: we release after the data is consumed later
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
                    if ( config.i2cReadDataBool()) {
                        config.i2cSetDataLow(); //lower data while clock is high
                        stepAtHand = 4;
                    } else {
                        logger.error("unable to be master, data line should be high, will try again.");
        
                        config.i2cDataOut(); //force the issue.
                        config.i2cSetDataHigh(); //force the issue.

                        taskAtHand = TASK_MASTER_START;
                        stepAtHand = 0;//so try again.
                        return;
                    }
                } else {
                    logger.error("clock stretching now.");
                    taskAtHand = TASK_MASTER_START;
                    stepAtHand = 0;//so try again.
                    return;//clock stretching, will come back to this state next cycle around                     
                }
                break;
            case 4:
                lastBit = 0;//data is set low and may be helpful for first bit
                config.i2cSetClockLow();
                stepAtHand = 5;
                break;//done
            case 5:
                stepAtHand = 1;
                taskAtHand = TASK_WRITE_BYTES;
                break;

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
                    //do not count as cycle top we just changed it above
                } else {
                    logger.trace("clock stretching now.");
                    //clock stretching, will come back to this state next cycle around
                }
                stepAtHand = 5;
                break;
            case 5: //done
                config.i2cSetDataHigh();
                stepAtHand = 0;
                taskAtHand = TASK_NONE;
                readRequest();
                break;
        }        
    }

    //TODO: add mulitple pipes out with array to map addr to the pipe for responses
    //TODO: produce one stage per grove component with its own schema
    

    
    private void writeBytes() {

        //System.out.println("write "+taskAtHand+" "+stepAtHand);

        switch (stepAtHand) {
            case 1:
            case 0:
                  //byteToSendPos starts with 8
                  if (0==(1 & (byteToSend >> (--byteToSendPos)))) {
                      //System.out.println("0 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      if (0!=lastBit) {
                        config.i2cSetDataLow();
                        lastBit = 0;
                        stepAtHand = 2;
                        break;
                      }
                  } else {
                      //System.out.println("1 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      if (1!=lastBit) {
                        config.i2cSetDataHigh();
                        lastBit = 1;
                        stepAtHand = 2;
                        break;
                      }
                  } 
                  //fall through if the value is already what we need.
            case 2:
                  config.i2cSetClockHigh();
                  cycleTops++;
                  stepAtHand = 3;
                  break;
            case 3:
                  if (config.i2cReadClockBool()) {
                      config.i2cSetClockLow();
                      //jump to 4 when byteToEndPos is zero else jump to 0 
                      stepAtHand = 4 & ((byteToSendPos-1)>>31);
                  } else {
                      logger.error("clock stretching now.");
                      //clock stretching, will come back to this state next cycle around
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
                    logger.error("clock stretching now.");
                    //clock stretching, will come back to this state next cycle around
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
                    
  //                  duration += System.nanoTime()-startTime;

                    //release the resources from the pipe for more data
                    Pipe.confirmLowLevelRead(activePipe, bytesToSendReleaseSize);
                    Pipe.releaseReadLock(activePipe);


                    
                    
                } else {       
                    
                    byteToSend = 0xFF & bytesToSendBacking[bytesToSendMask & bytesToSendPosition++];
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
