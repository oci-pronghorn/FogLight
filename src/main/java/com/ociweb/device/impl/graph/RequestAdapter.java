package com.ociweb.device.impl.graph;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class RequestAdapter {

    private Pipe<GroveRequestSchema> output;
    private AtomicBoolean aBool = new AtomicBoolean(false);
    
    protected RequestAdapter(Pipe<GroveRequestSchema> output) {
       this.output = output;
    }

    
    public boolean digitalForDuration(int connector, int duration, int value) {
        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            
            if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BUZZER_110)) { //TODO: this needs to be generic 
                
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BUZZER_110_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BUZZER_110_FIELD_DURATION_112, duration);
                                
                PipeWriter.publishWrites(output);
                
                return true;
            } else {
                return false;
            }
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }
    
   
    
    
    public boolean setServoPositionint(int connector, int position) {
        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            
            if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_SERVO_140)) {
                
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_SERVO_140_FIELD_CONNECTOR_141, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_SERVO_140_FIELD_POSITION_142, position);
                                
                PipeWriter.publishWrites(output);
                
                return true;
            } else {
                return false;
            }
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }

    private boolean enterBlockOk() {
       return aBool.compareAndSet(false, true);
    }
    
    private boolean exitBlockOk() {
        return aBool.compareAndSet(true, false);
    }
    

}
