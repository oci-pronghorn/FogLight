package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class CommandChannel {

    public final Pipe<?>[] outputPipes;
    protected final Pipe<TrafficOrderSchema> goPipe;
    protected final Pipe<MessagePubSub> messagePubSub;
    protected AtomicBoolean aBool = new AtomicBoolean(false);    
        
    protected CommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,  Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> goPipe) {
       this.outputPipes = new Pipe<?>[]{output,i2cOutput,messagePubSub,goPipe};
       this.goPipe = goPipe;
       this.messagePubSub = messagePubSub;
    }
    
    protected void publishGo(int count, int pipeIdx) {
        if(PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) {                 
            PipeWriter.writeInt(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, pipeIdx);
            PipeWriter.writeInt(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, count);
            PipeWriter.publishWrites(goPipe);
        } else {
            throw new UnsupportedOperationException("Was already check and should not have run out of space.");
        }
    }

    protected boolean enterBlockOk() {
        return aBool.compareAndSet(false, true);
    }
    
    protected boolean exitBlockOk() {
        return aBool.compareAndSet(true, false);
    }
    
    public abstract boolean block(int msDuration);
    public abstract boolean block(int connector, int msDuration);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean digitalSetValueAndBlock(int connector, int value, int msDuration);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean analogSetValueAndBlock(int connector, int value, int msDuration);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress);
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();

    public boolean subscribe(String topic, PubSubListener listener) {
        // TODO Auto-generated method stub
        
        return false;
    }

    public boolean unsubscribe(String topic, PubSubListener listener) {
        // TODO Auto-generated method stub
        
        return false;
    }

    public void openTopic(String string) {
        // TODO Auto-generated method stub
        
        //returns topicObject
    }

}