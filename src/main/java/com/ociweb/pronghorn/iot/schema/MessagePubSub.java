package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class MessagePubSub extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0xa0000000,0x80000000,0xc0200003,0xc0400003,0xa0000000,0x80000000,0xc0200003,0xc0400003,0xa0000000,0xb8000001,0xc0200003},
            (short)0,
            new String[]{"Subscribe","Topic","PipeIdx",null,"Unsubscribe","Topic","PipeIdx",null,"Publish","Topic","Payload",null},
            new long[]{100, 1, 2, 0, 101, 1, 2, 0, 103, 1, 3, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null},
            "MessagePubSub.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_SUBSCRIBE_100 = 0x00000000;
    public static final int MSG_SUBSCRIBE_100_FIELD_TOPIC_1 = 0x01000001;
    public static final int MSG_SUBSCRIBE_100_FIELD_PIPEIDX_2 = 0x00000003;
    public static final int MSG_UNSUBSCRIBE_101 = 0x00000004;
    public static final int MSG_UNSUBSCRIBE_101_FIELD_TOPIC_1 = 0x01000001;
    public static final int MSG_UNSUBSCRIBE_101_FIELD_PIPEIDX_2 = 0x00000003;
    public static final int MSG_PUBLISH_103 = 0x00000008;
    public static final int MSG_PUBLISH_103_FIELD_TOPIC_1 = 0x01000001;
    public static final int MSG_PUBLISH_103_FIELD_PAYLOAD_3 = 0x01C00003;
    
    
    public static final MessagePubSub instance = new MessagePubSub();
    
    private MessagePubSub() {
        super(FROM);
    }
        
}
