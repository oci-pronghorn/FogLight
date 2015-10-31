package com.ociweb.device.grove;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

//TODO: should be Grove specific NOT edision specific
public class GroveShieldV2RequestStage extends PronghornStage {

    public GroveShieldV2RequestStage(GraphManager gm, Pipe<GroveRequestSchema> requestPipe) {
        super(gm, requestPipe, NONE);
    }
    
        
    
    @Override
    public void startup() {

        
        
    }
    
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
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
