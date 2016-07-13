package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.util.Pool;

public class PayloadWriter extends DataOutputBlobWriter<MessagePubSub>{

    private final Pool<PayloadWriter> payloadWriterPool;
    private long key;
    
    public PayloadWriter(Pipe<MessagePubSub> p, Pool<PayloadWriter> payloadWriterPool) {
        super(p);
        this.payloadWriterPool = payloadWriterPool;
    }

    //package protected
    void setKey(long key) {
        this.key = key;
    }

}
