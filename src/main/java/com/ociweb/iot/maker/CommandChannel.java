package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;

public abstract class CommandChannel {

    private Pipe<GroveRequestSchema> output;
    private Pipe<I2CCommandSchema> i2cOutput;
    private Pipe<GoSchema> goPipe;
    private AtomicBoolean aBool = new AtomicBoolean(false);    
    private DataOutputBlobWriter<RawDataSchema> i2cWriter;  
    private int runningI2CCommandCount;
    
    protected CommandChannel(Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<GoSchema> goPipe) {
       this.output = output;
       this.i2cOutput = i2cOutput;  
       this.goPipe = goPipe;
      
    }

    
    public abstract boolean digitalBlock(int connector, int duration);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<RawDataSchema> i2cCommandOpen();
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();


}