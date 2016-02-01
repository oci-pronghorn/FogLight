package com.ociweb.device.grove.grovepi;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GrovePiI2CStageV2 extends PronghornStage {
    private static final int NS_PAUSE = 10*1000;
    
    private static final int TASK_NONE = 0;
    private static final int TASK_MASTER_START = 1;
    private static final int TASK_MASTER_STOP  = 2;
    private static final int TASK_WRITE_BYTES  = 3;
    
    private final Pipe<I2CCommandSchema> request;
    
    public final GroveConnectionConfiguration config;
    
    private int taskPhase = 0;
    
    public int cyclesToWait;
    public int byteToSend;
    
    //holds the same array as used by the Blob from the ring.
    private byte[] bytesToSendBacking; //set before send
    private int    bytesToSendRemaining;
    private int    bytesToSendPosition;
    private int    bytesToSendMask;
    private int    bytesToSendReleaseSize;    
    
    private static final int MAX_CONFIGURABLE_BYTES = 16;
    private int[] cyclesToWaitLookup = new int[MAX_CONFIGURABLE_BYTES];
    
    private void pause() {
        try {
            Thread.sleep(NS_PAUSE / 1000000, NS_PAUSE % 1000000);
        }
        
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void writeBit(boolean bit) {
        if (bit) config.i2cSetDataHigh();
        else config.i2cSetDataLow();
        config.i2cClockOut();
        
        config.i2cSetClockHigh();
        config.i2cClockIn();
        
        while (config.i2cReadClock() == 0) {
            System.out.println("Clock stretching in writeBit...");
        }

        config.i2cClockOut();
        config.i2cSetClockLow();
    }
    
    private void writeByte(int b) {
        for (int bit = 0; bit < 8; bit++) {
            writeBit((b & 0x80) != 0);
            b <<= 1;
        }
    }
    
    public GrovePiI2CStageV2(GraphManager gm, Pipe<I2CCommandSchema> request, GroveConnectionConfiguration config) {
        super(gm, request, NONE);
        
        this.request = request;
        this.config = config;
        
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, NS_PAUSE, this);
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);
    }
    
    @Override
    public void startup() {
        if (Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
//            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        }
        
        //Setup I2C.
        config.beginPinConfiguration();
        config.configurePinsForI2C();
        config.i2cClockOut();
        config.endPinConfiguration();
        config.i2cSetClockHigh();
        pause();
        config.i2cSetDataHigh();
        pause();
    }
    
    @Override
    public void run() {
        if (taskPhase == TASK_NONE) {
            readRequest();
        }
        
        switch (taskPhase) {
            case TASK_MASTER_START:
                masterStart();
                break;
            case TASK_WRITE_BYTES:
                masterWrite();
                break;
            case TASK_MASTER_STOP:
                masterStop();
                break;
        }
    }
    
    private void readRequest() {
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
                    
                    taskPhase = TASK_MASTER_START;
                    
                    cyclesToWait = bytesToSendPosition<MAX_CONFIGURABLE_BYTES ? cyclesToWaitLookup[bytesToSendPosition] : 0;            
                    byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
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
        config.i2cSetDataLow();
        
        config.i2cClockOut();
        config.i2cSetClockLow();
        
        taskPhase = TASK_WRITE_BYTES;
    }
    
    private void masterWrite() {
        writeByte(byteToSend);
        System.out.println("Sent 0x" + Integer.toHexString(byteToSend));
        if (--bytesToSendRemaining <= 0) {
            taskPhase = TASK_MASTER_STOP; //we are all done
            
            //release the resources from the pipe for more data
            Pipe.confirmLowLevelRead(request, bytesToSendReleaseSize);
            Pipe.releaseReads(request);
        } 
        
        else {
            byteToSend = 0xFF&bytesToSendBacking[bytesToSendMask&bytesToSendPosition++];
        }
    }
    
    private void masterStop() {
       config.i2cSetDataLow();
       
       config.i2cClockIn();
       while (config.i2cReadClock() == 0) {
           System.out.println("Clock stretching in masterStop...");
       }
       
       config.i2cSetDataHigh();
       
       taskPhase = TASK_NONE;
    }
}
