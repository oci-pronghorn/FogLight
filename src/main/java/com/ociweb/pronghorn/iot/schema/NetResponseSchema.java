package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class NetResponseSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0x90000000,0xb8000000,0xc0200003},
		    (short)0,
		    new String[]{"Response","ConnectionId","Payload",null},
		    new long[]{101, 1, 3, 0},
		    new String[]{"global",null,null,null},
		    "NetResponse.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final NetResponseSchema instance = new NetResponseSchema();
    
    public static final int MSG_RESPONSE_101 = 0x00000000;
    public static final int MSG_RESPONSE_101_FIELD_CONNECTIONID_1 = 0x00800001;
    public static final int MSG_RESPONSE_101_FIELD_PAYLOAD_3 = 0x01C00003;
    
    protected NetResponseSchema() {
        super(FROM);
    }
        
}
