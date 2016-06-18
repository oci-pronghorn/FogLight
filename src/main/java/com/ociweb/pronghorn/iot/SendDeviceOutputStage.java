package com.ociweb.pronghorn.iot;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Blocker;

public class SendDeviceOutputStage extends PronghornStage {

	private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
    private static final short activeSize = (short)(1<<activeBits);
    
    
    private int[][]    movingAverageHistory;
    private int[]      lastPublished;
    private int[]       scriptConn;
    private int[]       scriptTask; 
    private IODevice[] 		scriptTwig;
    private static final short DO_BIT_Write    = 1;
    
    private Blocker blocker = new Blocker(16);// max of 16 pipes can be waiting with different times.
    
    private final Pipe<GroveRequestSchema>[] requestPipes;
    private final Hardware config;
    
    
    public SendDeviceOutputStage(GraphManager gm, Pipe<GroveRequestSchema>[] requestPipes, Hardware config) {
        super(gm, requestPipes, NONE);
        
        this.requestPipes = requestPipes;
        this.config = config;
        GraphManager.addNota(gm, GraphManager.PRODUCER, GraphManager.PRODUCER, this);  
        
    }
    
        
    
    @Override
    public void startup() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        
        int j = config.maxAnalogMovingAverage()-1;
        movingAverageHistory = new int[j][]; 
        while (--j>=0) {
            movingAverageHistory[j] = new int[activeSize];            
        }
        lastPublished = new int[activeSize];

        
        //before we setup the pins they must start in a known state
        //this is required for the ATD converters (eg any analog port usage)
               
        
        byte sliceCount = 0;
        
        //configure each sensor
        config.beginPinConfiguration();
        
        
        int i = config.digitalOutputs.length;
        while(--i >= 0){
        	config.configurePinsForDigitalOutput(config.digitalOutputs[i].connection);
        	System.out.println("configured output "+config.digitalOutputs[i].twig+" on connection "+config.digitalOutputs[i].connection);
        }
    	
    	
       i = config.pwmOutputs.length;
       while (--i>=0) {
            config.configurePinsForAnalogOutput(config.pwmOutputs[i].connection);
       }        
    
       config.endPinConfiguration();

    }
    
    
    @Override
    public void run() {

        long now = System.currentTimeMillis();
      
        int j = requestPipes.length;
        while (--j>=0) {
            processPipe(requestPipes[j],now);
            
        }
        
    }

    private void processPipe(Pipe<GroveRequestSchema> requestPipe, long now) {
        
        
        //release all those that no longer need to wait
        int next = -1;        
        do {
            next = blocker.nextReleased(now, -1);        
            if (-1 != next) {
                System.out.println("now release "+next);
            }
            
        } while (-1 != next);
        
        while (Pipe.hasContentToRead(requestPipe) && !blocker.isBlocked(Pipe.peekInt(requestPipe, 1)) ) {
            
            //read the messages.
            int msg = Pipe.takeMsgIdx(requestPipe);
            
            switch (msg) {
                case GroveRequestSchema.MSG_DIGITALSET_110:
                {
                    int connector = Pipe.takeValue(requestPipe);    
                    if (blocker.isBlocked(connector)) {
                        throw new UnsupportedOperationException();
                    }
                    int value = Pipe.takeValue(requestPipe);
                    
                    config.digitalWrite(connector,value);
                           
                }   
                break;
                case GroveRequestSchema.MSG_BLOCK_220:
                {
                    int connector = Pipe.takeValue(requestPipe);
                    if (blocker.isBlocked(connector)) {
                        throw new UnsupportedOperationException();
                    }
                    int duration = Pipe.takeValue(requestPipe); 
                    
                    System.out.println("block "+connector+"  now "+now+" duration "+duration+" target time "+(now+duration));
                    
                    blocker.until(connector, now + (long)duration);
                    
                    Pipe.confirmLowLevelRead(requestPipe, Pipe.sizeOf(requestPipe, msg));
                    Pipe.releaseReadLock(requestPipe);
                    return;
                }   
            //    break;
                case GroveRequestSchema.MSG_ANALOGSET_140:
                { 
                    int connector = Pipe.takeValue(requestPipe);
                    if (blocker.isBlocked(connector)) {
                        throw new UnsupportedOperationException();
                    }
                    int value = Pipe.takeValue(requestPipe); 
            
                    //TODO: Ask Alex how we should support setting the period?
                    
                    config.analogWrite(connector, value);
    
                    
                }   
                break;    
                        
            }
            
            Pipe.confirmLowLevelRead(requestPipe, Pipe.sizeOf(requestPipe, msg));
            Pipe.releaseReadLock(requestPipe);
                        
        }
    }

    @Override
    public void shutdown() {
        
        
    }
    
    
    
}
