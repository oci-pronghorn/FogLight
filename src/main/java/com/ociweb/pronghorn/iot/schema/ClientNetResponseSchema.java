package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class ClientNetResponseSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400004,0x80000000,0x80000001,0xa8000000,0xc0200004},
		    (short)0,
		    new String[]{"SimpleResponse","HostId","UserId","Payload",null},
		    new long[]{200, 201, 202, 203, 0},
		    new String[]{"global",null,null,null,null},
		    "ClientNetResponse.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final ClientNetResponseSchema instance = new ClientNetResponseSchema();
    
    public static final int MSG_SIMPLERESPONSE_200 = 0x00000000;
    public static final int MSG_SIMPLERESPONSE_200_FIELD_HOSTID_201 = 0x00000001;
    public static final int MSG_SIMPLERESPONSE_200_FIELD_USERID_202 = 0x00000002;
    public static final int MSG_SIMPLERESPONSE_200_FIELD_PAYLOAD_203 = 0x01400003;
    
    protected ClientNetResponseSchema() {
        super(FROM);
    }
        
}
