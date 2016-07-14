package com.ociweb.iot.maker;

import java.io.IOException;

import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.util.Pool;

public class PayloadWriter {

    private final DataOutputBlobWriter<MessagePubSub> writer;
    private final CommandChannel commandChannel;
    private long key;
    private Pipe<MessagePubSub> pipe;
    
    public PayloadWriter(Pipe<MessagePubSub> p, CommandChannel commandChannel) {
        this.writer = new DataOutputBlobWriter<MessagePubSub>(p);
        this.commandChannel = commandChannel;                
        this.pipe = p;
    }
    
    public PayloadWriter writeInt(int value) {
        try {
            writer.writeInt(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
        
    public PayloadWriter writeString(CharSequence value) {
        try {
            writer.writeUTF(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void publish() {
        
        writer.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
        PipeWriter.publishWrites(pipe);
        
        int count = 1;
        commandChannel.publishGo(count,commandChannel.subPipeIdx);        
        commandChannel.payloadWriterPool.release(key);
        
    }

    void openField(long key) {
        this.key = key;
        DataOutputBlobWriter.openField(writer);
    }

}
