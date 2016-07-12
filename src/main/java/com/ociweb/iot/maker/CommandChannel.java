package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class CommandChannel {

    public final Pipe<?>[] outputPipes;
    
    protected CommandChannel(GraphManager gm, Pipe<?> ... outputPipes) {
       this.outputPipes = outputPipes;
    }
    
    public abstract boolean digitalBlock(int connector, int duration);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress);
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();

}