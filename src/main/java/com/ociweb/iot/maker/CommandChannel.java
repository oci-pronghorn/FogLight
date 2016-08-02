package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Pool;

public abstract class CommandChannel {

    protected final Pipe<?>[] outputPipes;
    protected final Pipe<TrafficOrderSchema> goPipe;
    protected final Pipe<MessagePubSub> messagePubSub;
    protected final Pipe<I2CCommandSchema> i2cOutput;
    protected final Pipe<GroveRequestSchema> output;
    
    protected AtomicBoolean aBool = new AtomicBoolean(false);   

    protected DataOutputBlobWriter<I2CCommandSchema> i2cWriter;  
    protected int runningI2CCommandCount;
    
    private Object listener;

    //TODO: need to set this as a constant driven from the known i2c devices and the final methods, what is the biggest command sequence?
    protected final int maxCommands = 50;
    
    private long topicKeyGen;
    
    Pool<PayloadWriter> payloadWriterPool;
    private final int maxOpenTopics = 1;
    
    protected final int pinPipeIdx = 0; 
    protected final int i2cPipeIdx = 1;
    protected final int subPipeIdx = 2;
    
        
    protected CommandChannel(GraphManager gm, 
                             Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,  Pipe<MessagePubSub> messagePubSub,  //avoid adding more and see how they can be combined.
                             Pipe<TrafficOrderSchema> goPipe) {
       this.outputPipes = new Pipe<?>[]{output,i2cOutput,messagePubSub,goPipe};
       this.goPipe = goPipe;
       this.messagePubSub = messagePubSub;
       this.i2cOutput = i2cOutput;
                             
       if (Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands >= this.i2cOutput.sizeOfSlabRing) {
           throw new UnsupportedOperationException("maxCommands too large or pipe is too small, pipe size must be at least "+(Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands));
       }
       
       
       this.output = output;
    }
    

    void setListener(Object listener) {
        if (null != this.listener) {
            throw new UnsupportedOperationException("Bad Configuration, A CommandChannel can only be held and used by a single listener lambda/class");
        }
        this.listener = listener;
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
    
    public abstract boolean block(long msDuration);
    public abstract boolean block(int connector, long durationMilli);
    public abstract boolean blockUntil(int connector, long time);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean digitalSetValueAndBlock(int connector, int value, long durationMilli);
    public abstract boolean digitalPulse(int connector);
    public abstract boolean digitalPulse(int connector, long durationMilli);
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean analogSetValueAndBlock(int connector, int value, long durationMilli);

    public DataOutputBlobWriter<I2CCommandSchema> i2cCommandOpen(int targetAddress) {       
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {

            if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);

                if (null==i2cWriter) {
                    //TODO: add init method that we we will call from stage to do this.
                    i2cWriter = new DataOutputBlobWriter<I2CCommandSchema>(i2cOutput);//hack for now until we can get this into the scheduler TODO: nathan follow up.
                }
                 
                DataOutputBlobWriter.openField(i2cWriter);
                return i2cWriter;
            } else {
                throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
            }
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }
    


    
    public void i2cDelay(int targetAddress, int durationMillis) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (++runningI2CCommandCount > maxCommands) {
                throw new UnsupportedOperationException("too many commands, found "+runningI2CCommandCount+" but only left room for "+maxCommands);
            }
        
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20)) {

                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_CONNECTOR_11, targetAddress);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_ADDRESS_12, targetAddress);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_DURATION_13, durationMillis);

                PipeWriter.publishWrites(i2cOutput);

            }else {
                throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
            }    
            
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }
    
    public boolean i2cIsReady() {
        
        return PipeWriter.hasRoomForWrite(goPipe) &&         
                PipeWriter.hasRoomForFragmentOfSize(i2cOutput, Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands);
       
    }
    

    public void i2cFlushBatch() {        
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            publishGo(runningI2CCommandCount,i2cPipeIdx);
            runningI2CCommandCount = 0;

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }



    public void i2cCommandClose() {  
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (++runningI2CCommandCount > maxCommands) {
                throw new UnsupportedOperationException("too many commands, found "+runningI2CCommandCount+" but only left room for "+maxCommands);
            }
            
            DataOutputBlobWriter.closeHighLevelField(i2cWriter, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            PipeWriter.publishWrites(i2cOutput);
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }
    
    public boolean subscribe(CharSequence topic) {
        return subscribe(topic, (PubSubListener)listener);
    }
    
    public boolean subscribe(CharSequence topic, PubSubListener listener) {
        if (PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100)) {
            
            PipeWriter.writeInt(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4, System.identityHashCode(listener));
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_TOPIC_1, topic);
            
            PipeWriter.publishWrites(messagePubSub);
            
            publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    }

    public boolean unsubscribe(CharSequence topic) {
        return unsubscribe(topic, (PubSubListener)listener);
    }
    
    public boolean unsubscribe(CharSequence topic, PubSubListener listener) {
        if (PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_UNSUBSCRIBE_101)) {
            
            PipeWriter.writeInt(messagePubSub, MessagePubSub.MSG_SUBSCRIBE_100_FIELD_SUBSCRIBERIDENTITYHASH_4, System.identityHashCode(listener));
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_UNSUBSCRIBE_101_FIELD_TOPIC_1, topic);
            
            PipeWriter.publishWrites(messagePubSub);
            
            publishGo(1,subPipeIdx);
            
            return true;
        }        
        return false;
    }

    
    public PayloadWriter openTopic(CharSequence topic) {
        
        if (PipeWriter.tryWriteFragment(messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
        
            if (null==payloadWriterPool) {
                lazyInitOfPool(); //must be after the listener has init all the pipes., TODO: could be done when we assign the lister, if we do.
            }
            
            long key = ++topicKeyGen;
            PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);            
            PayloadWriter pw = payloadWriterPool.get(key);            
            pw.openField(key);            
                        
            return pw;
            
        } else {
            //breaks fluent api use because there is no place to write the data.
            //makers/students will have a lesson where we do new Optional(openTopic("topic"))  then  ifPresent() to only send when we can
            return null;
        }
    }


    private void lazyInitOfPool() {
        PayloadWriter[] members = new PayloadWriter[maxOpenTopics];       
        payloadWriterPool = new Pool<PayloadWriter>(members);
        int m = maxOpenTopics;
        while (--m >= 0) {
            members[m] = new PayloadWriter(messagePubSub, this);
        }
    }



}