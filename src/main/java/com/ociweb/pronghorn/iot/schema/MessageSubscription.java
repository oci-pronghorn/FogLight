package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class MessageSubscription extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0xa0000000,0xb8000001,0xc0200003},
            (short)0,
            new String[]{"Publish","Topic","Payload",null},
            new long[]{103, 1, 3, 0},
            new String[]{"global",null,null,null},
            "MessageSubscriber.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_PUBLISH_103 = 0x00000000;
    public static final int MSG_PUBLISH_103_FIELD_TOPIC_1 = 0x01000001;
    public static final int MSG_PUBLISH_103_FIELD_PAYLOAD_3 = 0x01C00003;
    
    public static final MessageSubscription instance = new MessageSubscription();
    
    private MessageSubscription() {
        super(FROM);
    }
        
}
