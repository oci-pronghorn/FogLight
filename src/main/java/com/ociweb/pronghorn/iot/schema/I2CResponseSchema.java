package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class I2CResponseSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400005,0x80000000,0x90000000,0x80000001,0xb8000000,0xc0200005},
            (short)0,
            new String[]{"Response","Address","Time","Register","ByteArray",null},
            new long[]{10, 11, 13, 14, 12, 0},
            new String[]{"global",null,null,null,null,null},
            "I2CResponseSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CResponseSchema instance = new I2CResponseSchema();
    
    public static final int MSG_RESPONSE_10 = 0x00000000;
    public static final int MSG_RESPONSE_10_FIELD_ADDRESS_11 = 0x00000001;
    public static final int MSG_RESPONSE_10_FIELD_TIME_13 = 0x00800002;
    public static final int MSG_RESPONSE_10_FIELD_REGISTER_14 = 0x00000004;
    public static final int MSG_RESPONSE_10_FIELD_BYTEARRAY_12 = 0x01C00005;
    
    protected I2CResponseSchema() {
        super(FROM);
    }
        
}
