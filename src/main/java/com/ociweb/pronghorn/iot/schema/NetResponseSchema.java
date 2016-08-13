package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class NetResponseSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0x80000000,0xa8000000,0xb8000001,0xc0200004},
		    (short)0,
		    new String[]{"Response","Verb","URL","Payload",null},
		    new long[]{101, 1, 2, 3, 0},
		    new String[]{"global",null,null,null,null},
		    "NetResponse.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final NetResponseSchema instance = new NetResponseSchema();
    
    public static final int MSG_RESPONSE_101 = 0x00000000;
    public static final int MSG_RESPONSE_101_FIELD_VERB_1 = 0x00000001;
    public static final int MSG_RESPONSE_101_FIELD_URL_2 = 0x01400002;
    public static final int MSG_RESPONSE_101_FIELD_PAYLOAD_3 = 0x01C00004;
    
    protected NetResponseSchema() {
        super(FROM);
    }
        
}
