package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class NetRequestSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400002,0xa8000000,0xc0200002,0xc0400003,0xa8000000,0xb8000001,0xc0200003},
		    (short)0,
		    new String[]{"HTTPGet","URL",null,"HTTPPost","URL","Payload",null},
		    new long[]{100, 2, 0, 101, 2, 3, 0},
		    new String[]{"global",null,null,"global",null,null,null},
		    "NetRequest.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final NetRequestSchema instance = new NetRequestSchema();
    
    public static final int MSG_HTTPGET_100 = 0x00000000;
    public static final int MSG_HTTPGET_100_FIELD_URL_2 = 0x01400001;
    public static final int MSG_HTTPPOST_101 = 0x00000003;
    public static final int MSG_HTTPPOST_101_FIELD_URL_2 = 0x01400001;
    public static final int MSG_HTTPPOST_101_FIELD_PAYLOAD_3 = 0x01C00003;
    
    protected NetRequestSchema() {
        super(FROM);
    }
        
}
