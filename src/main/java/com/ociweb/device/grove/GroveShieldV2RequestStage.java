package com.ociweb.device.grove;

import com.ociweb.device.config.GroveConnectionConfiguration;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

//TODO: should be Grove specific NOT edision specific
public class GroveShieldV2RequestStage extends PronghornStage {

    private final Pipe<GroveRequestSchema> requestPipe;
    private final GroveConnectionConfiguration config;
    
    public GroveShieldV2RequestStage(GraphManager gm, Pipe<GroveRequestSchema> requestPipe, GroveConnectionConfiguration config) {
        super(gm, requestPipe, NONE);
        
        this.requestPipe = requestPipe;
        this.config = config;
        
    }
    
        
    
    @Override
    public void startup() {

        
    //  i = config.digitalOutputs.length;
    //  while (--i>=0) {
//          configDigitalOutput(config.digitalOutputs[i]); //take from pipe and write, get type and field from pipe
//          
//          script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&config.digitalOutputs[i])<<SHIFT_DO_PORT) |
//                                              ((MASK_DO_JOB&DO_DATA_WRITE)<<SHIFT_DO_JOB );
    //  }
        
    }
    
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        while (Pipe.hasContentToRead(requestPipe)) {
            
            //read the messages.
            int msg = Pipe.takeMsgIdx(requestPipe);
            
            switch (msg) {
                case GroveRequestSchema.MSG_BUZZER_110:
                    
                    
                break;
                case GroveRequestSchema.MSG_RELAY_120:
       
                    
                break;
                case GroveRequestSchema.MSG_SERVO_140:
               
                    
                break;    
                        
            }
            
            
        }
        
        
    }

    @Override
    public void shutdown() {
        
        
    }
    
    
//  i = config.pwmOutputs.length;
//  while (--i>=0) {
//      configPWM(config.pwmOutputs[i]); //take from pipe and write, get type and field from pipe
//      
//      script[reverseBits(sliceCount++)] = ((MASK_DO_PORT&config.pwmOutputs[i])<<SHIFT_DO_PORT) |
//                                          ((MASK_DO_JOB&DO_DATA_WRITE)<<SHIFT_DO_JOB );
//  }

    
    
}
