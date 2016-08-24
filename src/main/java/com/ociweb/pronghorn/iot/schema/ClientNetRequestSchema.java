package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class ClientNetRequestSchema extends MessageSchema {

	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0x90000000,0xa8000000,0xc0200003},
		    (short)0,
		    new String[]{"SimpleRequest","ConnectionId","Payload",null},
		    new long[]{100, 101, 103, 0},
		    new String[]{"global",null,null,null},
		    "ClientNetRequest.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
    
    public static final ClientNetRequestSchema instance = new ClientNetRequestSchema();
    
    public static final int MSG_SIMPLEREQUEST_100 = 0x00000000;
    public static final int MSG_SIMPLEREQUEST_100_FIELD_CONNECTIONID_101 = 0x00800001;
    public static final int MSG_SIMPLEREQUEST_100_FIELD_PAYLOAD_103 = 0x01400003;
    
    protected ClientNetRequestSchema() {
        super(FROM);
    }
        
}
