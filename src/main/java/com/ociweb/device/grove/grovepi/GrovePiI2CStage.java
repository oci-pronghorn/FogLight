package com.ociweb.device.grove.grovepi;

import java.util.Arrays;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GrovePiI2CStage extends PronghornStage {

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
        
    private static final int MAX_CONFIGURABLE_BYTES = 16;
    private int[] cyclesToWaitLookup = new int[MAX_CONFIGURABLE_BYTES];
    
    private int bitFromBus;
    
    private static final int TASK_NONE = 0;
    private static final int TASK_MASTER_START = 1;
    private static final int TASK_MASTER_STOP  = 2;
    private static final int TASK_WRITE_BYTES  = 3;
    
    public final GroveConnectionConfiguration config;
    
    private final Pipe<I2CCommandSchema> request;
    private final Pipe<I2CCommandSchema> response;
    
    private static final int NS_PAUSE = 10*1000;
    
    public GrovePiI2CStage(GraphManager gm, Pipe<I2CCommandSchema> request, GroveConnectionConfiguration config) {
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
            Thread.sleep(NS_PAUSE/1000000,NS_PAUSE%1000000);
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
           config.i2cSetClockHigh();
           pause();
           config.i2cSetDataHigh();
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
        
    }

    @Override
    public void run() {

        //required wait based on slave device needs
        if (cyclesToWait>0) {
            cyclesToWait--;
            return;
        }
        
        if (TASK_NONE == taskAtHand) {
//            System.out.println("read request");
            readRequest();//may populate task at hand
        }
        
        switch (taskAtHand) {
            case TASK_MASTER_START:
//                System.out.println("task master start "+stepAtHand);
                masterStart();
                break;
            case TASK_WRITE_BYTES:
//                System.out.println("task write bytes data "+stepAtHand);
                writeBytes();
                break;
            case TASK_MASTER_STOP:
//                System.out.println("task master stop "+stepAtHand);
                masterStop();

                break;
        }        
        //Must return after this point to ensure the clock speed is respected.
        config.progressLog(taskAtHand, stepAtHand, byteToSend);
    }

    //TODO: try dropping file and recreating to read.

    
    private void readRequest() {
        // TODO Auto-generated method stub
        if (Pipe.hasContentToRead(request)) {
            
            int msgId = Pipe.takeMsgIdx(request);
            if (msgId<0) {
                requestShutdown();
                return;
            }
            bytesToSendReleaseSize =  Pipe.sizeOf(request, msgId);
            
            switch(msgId) {
                case I2CCommandSchema.MSG_COMMAND_1:
                    int meta = Pipe.takeRingByteMetaData(request);
                    int len = Pipe.takeRingByteLen(request);
                    
                    bytesToSendBacking = Pipe.byteBackingArray(meta, request);
                    bytesToSendMask = Pipe.blobMask(request);
                    bytesToSendPosition = Pipe.bytePosition(meta, request, len);
                    bytesToSendRemaining = len;
                    
                    taskAtHand = TASK_MASTER_START;
                    stepAtHand = 0;
                    
                    cyclesToWait = bytesToSendPosition<MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;            
                    byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
                    byteToSendPos = 8;
                break;
                case I2CCommandSchema.MSG_SETDELAY_10:                    
                    int offset = Pipe.takeValue(request);
                    cyclesToWaitLookup[offset] = 1 + (Pipe.takeValue(request)/NS_PAUSE);
                    Pipe.confirmLowLevelRead(request,bytesToSendReleaseSize);
                    Pipe.releaseReads(request);
                break;    
            }
        }
    }

    private void masterStart() {
        switch (stepAtHand) {
            case 0:           
                config.i2cClockOut();
                config.i2cSetClockHigh();
                config.i2cClockIn();
                stepAtHand = 1;
                break;
            case 1:
                //TODO: must redo using the same logic as the ack read! needed for LCD text.
                if (0 == config.i2cReadClock()) {
                    System.out.println("failure, clock stretching");
                    return;//clock stretching, will come back to this state next cycle around 
                }         
                
                if (0 == config.i2cReadData()) {
                    System.out.println("failure, unable to be master, data line should be high");
                    taskAtHand = TASK_NONE;
                    return;
                }
                config.i2cSetDataLow(); //lower data while clock is high
                stepAtHand = 2;
                break;//pause
            case 2:
                if (0==config.i2cReadClock()) {                    
                   throw new RuntimeException("expected clock to be high");
                }
                config.i2cClockOut();
                config.i2cSetClockLow();
                
                if (1==config.i2cReadData()) {
                    System.out.println("failure, unable to be master SDA");
                    taskAtHand = TASK_NONE;
                    return;
                }
                if (1==config.i2cReadClock()) {
                    System.out.println("failure, unable to be master SCL");
                    taskAtHand = TASK_NONE;
                    return;
                }
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
                config.i2cSetDataLow();//swtich to high while clock is up.  
                stepAtHand = 1;
                break;
            case 1:
                config.i2cClockOut();
                config.i2cSetClockHigh();
                config.i2cClockIn();
                stepAtHand = 2;
                break;//pause
            case 2:
                if (0==config.i2cReadClock()) {
                    return;//clock stretching, will come back to this state next cycle around 
                }                
                config.i2cSetDataHigh();
                stepAtHand = 0;
                taskAtHand = TASK_NONE;
                
                //clear message specific delays, no need to keep going this slow.
                Arrays.fill(cyclesToWaitLookup, 0);
               
                break;
            default:
                throw new UnsupportedOperationException();
        }        
    }

    //TODO: confirm if set dataout /in drops line
    
    private void writeBytes() {
        switch (stepAtHand) {
            case 0:
                  //byteToSendPos starts with 8
                  if (0==(1 & (byteToSend >> (--byteToSendPos)))) {
//                      System.out.println("0 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      config.i2cSetDataLow();
                  } else {
//                      System.out.println("1 from pos "+byteToSendPos+" of "+Integer.toBinaryString(byteToSend));
                      config.i2cSetDataHigh();
                  } 
                  stepAtHand = 1;
                  break;
            case 1:
                  config.i2cClockOut();
                  config.i2cSetClockHigh();
                  config.i2cClockIn();
                  stepAtHand = 2;
                  break;//pause
            case 2:                
                  if (0==config.i2cReadClock()) {
                      System.out.println("Clock stretching wait..." + 2);
                     return;//clock stretching, will come back to this state next cycle around 
                  }
                  if (0!=(1 & (byteToSend >> byteToSendPos)) && config.i2cReadData()==0 ) {
                      throw new RuntimeException("Unable to confirm data set high");           
                  }
                  if (0==config.i2cReadClock()) {
                      System.out.println("Clock stretching evil..." + 2);
                      throw new UnsupportedOperationException("Clock should still be high");
                  }
                  config.i2cClockOut();
                  config.i2cSetClockLow();
      //            config.i2cDataOut();
                  
                  if (0 == byteToSendPos) {   
                      stepAtHand = 3; //now read the ack for this byte
                  } else { //we will start on the next bit.
                      stepAtHand = 0;
                  }
                  break;//pause
            case 3:
                config.i2cSetDataHigh(); //the state set here remains for the ack but why?       
                config.i2cDataIn();   //needed to open this right now so we can read the ack upon change.       
                stepAtHand = 4;
                break;
            case 4:                    
                config.i2cSetClockHigh();
                config.i2cClockIn();
                stepAtHand = 5;
                break;//pause
            case 5:
                if (0==config.i2cReadClock()) {
                    System.out.println("Clock stretching wait..." + 5);
                    return;//clock stretching, will come back to this state next cycle around 
                }
                if (0==config.i2cReadClock()) {

                    System.out.println("Clock stretching evil..." + 5);
                    throw new UnsupportedOperationException("Clock should still be high");
                }
                
                config.i2cClockOut();
                config.i2cSetClockLow();       
                stepAtHand = 6;
                break;//pause
            case 6:
                System.out.print(" sent 0x"+Integer.toHexString(byteToSend)+"  ");
                
                boolean ack = config.i2cReadAck();


                if (!ack) {
                    //What do we do up on ack?
                    //roll back and try again?
                    
                    
                } else {
                    System.out.println();
                }
                
                config.i2cDataOut();
             
                stepAtHand = 0;
                
                if (--bytesToSendRemaining<=0) {
                    taskAtHand = TASK_MASTER_STOP; //we are all done
                    
                    //release the resources from the pipe for more data
                    Pipe.confirmLowLevelRead(request,bytesToSendReleaseSize);
                    Pipe.releaseReads(request);
                } 
                
                else {           
                    cyclesToWait = bytesToSendPosition<MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;
                    byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
                    byteToSendPos = 8;
                    //System.out.println("sending byte "+Integer.toHexString(byteToSend));
                    //now back to zero to send the next byte 
                }
                break;//pause
            default:
                throw new UnsupportedOperationException();        
        }
    }

    private void readBit() {
        switch (stepAtHand) {
            case 0:
                  //wait for clock to be highwriteBytes
                  while (0 == config.i2cReadClock()) {
                    //This is a spinning block dependent upon the other end of i2c
                  } //writeValue
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
}
