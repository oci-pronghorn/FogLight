package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class GroveI2CSchema extends MessageSchema{

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400002,0xb8000000,0xc0200002},
            (short)0,
            new String[]{"I2C","Payload",null},
            new long[]{100, 101, 0},
            new String[]{"global",null,null},
            "GroveI2C.xml");
    
    public static final int MSG_I2C_100 = 0x0;
    public static final int MSG_I2C_100_FIELD_PAYLOAD_101 = 0x7000001;
    
    public static final GroveI2CSchema instance = new GroveI2CSchema();
    
    private GroveI2CSchema() {
        super(FROM);
    }
}
