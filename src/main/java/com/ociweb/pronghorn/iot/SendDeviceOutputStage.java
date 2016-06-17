package com.ociweb.pronghorn.iot;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SendDeviceOutputStage extends PronghornStage {

	private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
    private static final short activeSize = (short)(1<<activeBits);
    
    
    private int[][]    movingAverageHistory;
    private int[]      lastPublished;
    private int[]       scriptConn;
    private int[]       scriptTask; 
    private IODevice[] 		scriptTwig;
    private static final short DO_BIT_Write    = 1;
    
    
    
    private final Pipe<GroveRequestSchema>[] requestPipes;
    private final Hardware config;
    
    
    public SendDeviceOutputStage(GraphManager gm, Pipe<GroveRequestSchema>[] requestPipes, Hardware config) {
        super(gm, requestPipes, NONE);
        
        this.requestPipes = requestPipes;
        this.config = config;
        GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, 10*1000*1000, this);
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
      
        int j = requestPipes.length;
        while (--j>=0) {
            processPipe(requestPipes[j]);
            
        }
        
    }

    private void processPipe(Pipe<GroveRequestSchema> requestPipe) {
        while (Pipe.hasContentToRead(requestPipe)) {
            
            //read the messages.
            int msg = Pipe.takeMsgIdx(requestPipe);
            
            switch (msg) {
                case GroveRequestSchema.MSG_DIGITALSET_110:
                {
                    int connector = Pipe.takeValue(requestPipe);
                    int value = Pipe.takeValue(requestPipe);     
                    
                    
                    config.digitalWrite(connector,value);
                    
       
                }   
                break;
                case GroveRequestSchema.MSG_DIGITALSET_120:
                {
                    int connector = Pipe.takeValue(requestPipe);
                    int value = Pipe.takeValue(requestPipe);
                    int duration = Pipe.takeValue(requestPipe); 
                    
                    //TODO write something to device
                    
                }   
                break;
                case GroveRequestSchema.MSG_ANALOGSET_140:
                { 
                    int connector = Pipe.takeValue(requestPipe);
                    int value = Pipe.takeValue(requestPipe); 
            
                    config.analogWrite(connector, value);
                    //TODO write something to device
                    
                }   
                break;    
                //shutodown? needs all pipes to agree???
                
                        
            }
            
            Pipe.confirmLowLevelRead(requestPipe, Pipe.sizeOf(requestPipe, msg));
            Pipe.releaseReadLock(requestPipe);
            
            
        }
    }

    @Override
    public void shutdown() {
        
        
    }
    
    
    
}
