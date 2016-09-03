package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class NetRequestSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400005,0x80000000,0xa8000000,0xa8000001,0x80000001,0xc0200005,0xc0400006,0x80000000,0xa8000000,0xa8000001,0xb8000002,0x80000001,0xc0200006},
		    (short)0,
		    new String[]{"HTTPGet","Port","Host","Path","Listener",null,"HTTPPost","Port","Host","Path","Payload","Listener",null},
		    new long[]{100, 1, 2, 3, 10, 0, 101, 1, 2, 3, 5, 10, 0},
		    new String[]{"global",null,null,null,null,null,"global",null,null,null,null,null,null},
		    "NetRequest.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final NetRequestSchema instance = new NetRequestSchema();
    
    public static final int MSG_HTTPGET_100 = 0x00000000;
    public static final int MSG_HTTPGET_100_FIELD_PORT_1 = 0x00000001;
    public static final int MSG_HTTPGET_100_FIELD_HOST_2 = 0x01400002;
    public static final int MSG_HTTPGET_100_FIELD_PATH_3 = 0x01400004;
    public static final int MSG_HTTPGET_100_FIELD_LISTENER_10 = 0x00000006;
    
    public static final int MSG_HTTPPOST_101 = 0x00000006;
    public static final int MSG_HTTPPOST_101_FIELD_PORT_1 = 0x00000001;
    public static final int MSG_HTTPPOST_101_FIELD_HOST_2 = 0x01400002;
    public static final int MSG_HTTPPOST_101_FIELD_PATH_3 = 0x01400004;
    public static final int MSG_HTTPPOST_101_FIELD_PAYLOAD_5 = 0x01C00006;
    public static final int MSG_HTTPPOST_101_FIELD_LISTENER_10 = 0x00000008;
    
    protected NetRequestSchema() {
        super(FROM);
    }
        
}
