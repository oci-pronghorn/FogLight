package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.util.Pool;

public class PayloadWriter extends DataOutputBlobWriter<MessagePubSub>{

    private final Pool<PayloadWriter> payloadWriterPool;
    private long key;
    private Pipe<MessagePubSub> pipe;
    
    public PayloadWriter(Pipe<MessagePubSub> p, Pool<PayloadWriter> payloadWriterPool) {
        super(p);
        this.payloadWriterPool = payloadWriterPool;
        this.pipe = p;
    }

    //package protected
    void setKey(long key) {
        this.key = key;
    }
    
    //TOOD: the write methods must all return this object and they do not.
    

    public void publish() {
        
        closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
        PipeWriter.publishWrites(pipe);
        
        payloadWriterPool.release(key);
        
    }

}
