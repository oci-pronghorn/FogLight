package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.RawDataSchema;
public class I2CCommandSchema extends RawDataSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0x80000000,0xb8000000,0xc0200003,0xc0400003,0x80000000,0x90000000,0xc0200003,0xc0400003,0x80000000,0x90000001,0xc0200003,0xc0400002,0x90000000,0xc0200002,0xc0400002,0x90000001,0xc0200002},
            (short)0,
            new String[]{"Command","Address","ByteArray",null,"BlockConnectionMS","Address","Duration",null,"BlockConnectionUntil","Address","TimeMS",null,"BlockChannelMS","Duration",null,"BlockChannelUntil","TimeMS",null},
            new long[]{7, 12, 2, 0, 20, 12, 13, 0, 21, 12, 14, 0, 22, 13, 0, 23, 14, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,"global",null,null},
            "I2CCommandSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CCommandSchema instance = new I2CCommandSchema();
    
    public static final int MSG_COMMAND_7 = 0x00000000;
    public static final int MSG_COMMAND_7_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_COMMAND_7_FIELD_BYTEARRAY_2 = 0x01C00002;
    
    public static final int MSG_BLOCKCONNECTIONMS_20 = 0x00000004;
    public static final int MSG_BLOCKCONNECTIONMS_20_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_BLOCKCONNECTIONMS_20_FIELD_DURATION_13 = 0x00800002;
    
    public static final int MSG_BLOCKCONNECTIONUNTIL_21 = 0x00000008;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12 = 0x00000001;
    public static final int MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14 = 0x00800002;
    
    public static final int MSG_BLOCKCHANNELMS_22 = 0x0000000C;
    public static final int MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13 = 0x00800001;
    public static final int MSG_BLOCKCHANNELUNTIL_23 = 0x0000000F;
    public static final int MSG_BLOCKCHANNELUNTIL_23_FIELD_TIMEMS_14 = 0x00800001;
    
    protected I2CCommandSchema() {
        super(FROM);
    }
        
}
