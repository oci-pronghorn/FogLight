package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public final Pipe<?>[] outputPipes;
    protected final Pipe<TrafficOrderSchema> goPipe;
    protected final Pipe<MessagePubSub> messagePubSub;
    protected AtomicBoolean aBool = new AtomicBoolean(false);   
      
    private Object listener;

    private long topicKeyGen;
    
    Pool<PayloadWriter> payloadWriterPool;
    private final int maxOpenTopics = 1;
    
    protected final int pinPipeIdx = 0; 
    protected final int i2cPipeIdx = 1;
    protected final int subPipeIdx = 2;
    
        
    protected CommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,  Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> goPipe) {
       this.outputPipes = new Pipe<?>[]{output,i2cOutput,messagePubSub,goPipe};
       this.goPipe = goPipe;
       this.messagePubSub = messagePubSub;
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
    public abstract boolean block(int connector, long msDuration);
    public abstract boolean blockUntil(int connector, long time);
    public abstract boolean digitalSetValue(int connector, int value);
    public abstract boolean digitalSetValueAndBlock(int connector, int value, long msDuration);
    public abstract boolean digitalPulse(int connector); //TOOD: add duration, what is the expected accuracy of this?
    public abstract boolean analogSetValue(int connector, int value);
    public abstract boolean analogSetValueAndBlock(int connector, int value, long msDuration);
    public abstract boolean i2cIsReady();
    public abstract DataOutputBlobWriter<I2CCommandSchema> i2cCommandOpen(int targetAddress);
    public abstract void i2cCommandClose();
    public abstract boolean i2cFlushBatch();

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