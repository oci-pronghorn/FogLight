package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class CommandChannel {
 
    protected final CommandStage privateStage;
    
    
    protected CommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<TrafficOrderSchema> goPipe) {
      
        privateStage = new CommandStage(gm, output, i2cOutput, goPipe);
    }
    
    public abstract boolean digitalBlock(int connector, int duration);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress);
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();


    private class CommandStage extends PronghornStage {

        protected CommandStage(GraphManager graphManager, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<TrafficOrderSchema> goPipe) {
            super(graphManager, NONE, join(output, i2cOutput, goPipe));
            //never schedule
            GraphManager.addNota(graphManager, GraphManager.UNSCHEDULED, GraphManager.UNSCHEDULED, this);
        }

        @Override
        public void run() {
            //never scheduled
        }
        
    }
}