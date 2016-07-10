package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class CommandChannel {

    public static Class<CommandChannelStage> stageClass = CommandChannelStage.class;
    
    protected CommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<TrafficOrderSchema> goPipe) {
              //never schedule
        GraphManager.addNota(gm, GraphManager.UNSCHEDULED, GraphManager.UNSCHEDULED, new CommandChannelStage(gm, output, i2cOutput, goPipe));
    }
    
    public abstract boolean digitalBlock(int connector, int duration);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress);
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();

    

    private class CommandChannelStage extends PronghornStage {

        protected CommandChannelStage(GraphManager graphManager, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<TrafficOrderSchema> goPipe) {
            super(graphManager, NONE, join(output, i2cOutput, goPipe));
        }

        @Override
        public void startup() {
            //at this point we know the pipes are ready for use so mark it as ready for use.
        }
                
        @Override
        public void run() {
            //never scheduled
            throw new UnsupportedOperationException("this should never be called, check the scheduler");
        }
        
    }
}