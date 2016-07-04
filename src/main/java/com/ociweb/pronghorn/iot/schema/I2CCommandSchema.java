package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.RawDataSchema;
public class I2CCommandSchema extends RawDataSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0x80000000,0xb8000000,0xc0200003,0xc0400003,0x80000000,0x90000000,0xc0200003,0xc0400004,0x80000000,0xb8000000,0x90000000,0xc0200004},
            (short)0,
            new String[]{"Command","Address","ByteArray",null,"Block","Address","Duration",null,"CommandAndBlock","Address","ByteArray","Duration",null},
            new long[]{7, 12, 2, 0, 10, 12, 13, 0, 11, 12, 2, 13, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null,null},
            "I2CCommandSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CCommandSchema instance = new I2CCommandSchema();
    
    public static final int MSG_COMMAND_7 = 0x00000000;
    public static final int MSG_COMMAND_7_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_COMMAND_7_FIELD_BYTEARRAY_2 = 0x01C00002;
    
    public static final int MSG_BLOCK_10 = 0x00000004;
    public static final int MSG_BLOCK_10_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_BLOCK_10_FIELD_DURATION_13 = 0x00800002;
    
    public static final int MSG_COMMANDANDBLOCK_11 = 0x00000008;
    public static final int MSG_COMMANDANDBLOCK_11_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_COMMANDANDBLOCK_11_FIELD_BYTEARRAY_2 = 0x01C00002;
    public static final int MSG_COMMANDANDBLOCK_11_FIELD_DURATION_13 = 0x00800004;
    
    protected I2CCommandSchema() {
        super(FROM);
    }
        
}
