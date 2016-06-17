package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class RequestAdapter {

    private Pipe<GroveRequestSchema> output;
    private AtomicBoolean aBool = new AtomicBoolean(false);
    
    protected RequestAdapter(Pipe<GroveRequestSchema> output) {
       this.output = output;
    }

    
    public boolean digitalForDuration(int connector, int duration, int value) {
        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            
            if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_120)) { //TODO: this needs to be generic 
                
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_120_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_120_FIELD_DURATION_113, duration);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_120_FIELD_VALUE_112, value);
                                                
                PipeWriter.publishWrites(output);
                
                return true;
            } else {
                return false;
            }
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }
    
    public boolean digitalSetValue(int connector, int value) {
        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            
            if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) { //TODO: this needs to be generic 
                
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, value);
                                                
                PipeWriter.publishWrites(output);
                
                return true;
            } else {
                return false;
            }
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }
    
    
    public boolean analogSetValue(int connector, int value) {
        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            
            if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_ANALOGSET_140)) {
                
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142, value);
                                
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
