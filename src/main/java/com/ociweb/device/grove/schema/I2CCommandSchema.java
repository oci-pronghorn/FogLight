package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.RawDataSchema;
public class I2CCommandSchema extends RawDataSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400002,0xb8000000,0xc0200002,0xc0400003,0x80000000,0x80000001,0xc0200003},
            (short)0,
            new String[]{"Command","ByteArray",null,"SetDelay","BeforeByteOffset","DelayInNanoSeconds",null},
            new long[]{1, 2, 0, 10, 12, 13, 0},
            new String[]{"global",null,null,"global",null,null,null},
            "I2CCommandSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CCommandSchema instance = new I2CCommandSchema();
    
    public static final int MSG_COMMAND_1 = 0x00000000;
    public static final int MSG_COMMAND_1_FIELD_BYTEARRAY_2 = 0x01C00001;
    public static final int MSG_SETDELAY_10 = 0x00000003;
    public static final int MSG_SETDELAY_10_FIELD_BEFOREBYTEOFFSET_12 = 0x00000001;
    public static final int MSG_SETDELAY_10_FIELD_DELAYINNANOSECONDS_13 = 0x00000002;
    
    protected I2CCommandSchema() {
        super(FROM);
    }
        
}
