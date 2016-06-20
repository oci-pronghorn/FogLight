package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveRequestSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc1400003,0x80200000,0x80000001,0xc1200003,0xc1400003,0x80200000,0x80000002,0xc1200003,0xc0400002,0x80000003,0xc0200002,0xc1400003,0x80200004,0x80000005,0xc1200003},
            (short)0,
            new String[]{"DigitalSet","Connector","Value",null,"Block","Connector","Duration",null,"I2CWrite","MessageCount",null,"AnalogSet","Connector","Value",null},
            new long[]{110, 111, 112, 0, 220, 111, 113, 0, 400, 410, 0, 140, 141, 142, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,"global",null,null,null},
            "GroveRequest.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_DIGITALSET_110 = 0x00000000;
    public static final int MSG_DIGITALSET_110_FIELD_CONNECTOR_111 = 0x00000001;
    public static final int MSG_DIGITALSET_110_FIELD_VALUE_112 = 0x00000002;
    
    public static final int MSG_BLOCK_220 = 0x00000004;
    public static final int MSG_BLOCK_220_FIELD_CONNECTOR_111 = 0x00000001;
    public static final int MSG_BLOCK_220_FIELD_DURATION_113 = 0x00000002;
    
    public static final int MSG_I2CWRITE_400 = 0x00000008;
    public static final int MSG_I2CWRITE_400_FIELD_MESSAGECOUNT_410 = 0x00000001;
    
    public static final int MSG_ANALOGSET_140 = 0x0000000B;
    public static final int MSG_ANALOGSET_140_FIELD_CONNECTOR_141 = 0x00000001;
    public static final int MSG_ANALOGSET_140_FIELD_VALUE_142 = 0x00000002;

    
    public static final GroveRequestSchema instance = new GroveRequestSchema();
    
    private GroveRequestSchema() {
        super(FROM);
    }
        
}
