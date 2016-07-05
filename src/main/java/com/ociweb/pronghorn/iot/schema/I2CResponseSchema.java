package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.RawDataSchema;
public class I2CResponseSchema extends RawDataSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0x80000000,0xb8000000,0xc0200003},
            (short)0,
            new String[]{"Response","Address","ByteArray",null},
            new long[]{10, 11, 12, 0},
            new String[]{"global",null,null,null},
            "I2CResponseSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CResponseSchema instance = new I2CResponseSchema();
    
    public static final int MSG_RESPONSE_10 = 0x00000000;
    public static final int MSG_RESPONSE_10_FIELD_ADDRESS_11 = 0x00000001;
    public static final int MSG_RESPONSE_10_FIELD_BYTEARRAY_12 = 0x01C00002;
    
    protected I2CResponseSchema() {
        super(FROM);
    }
        
}
