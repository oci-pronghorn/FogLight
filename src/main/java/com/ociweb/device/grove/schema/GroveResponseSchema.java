package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveResponseSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400002,0x90000000,0xc0200002,0xc1400004,0x80200000,0x80000001,0x80000002,0xc1200004,0xc1400004,0x80200003,0x80000004,0x80000005,0xc1200004,0xc1400004,0x80200006,0x80000007,0x80000008,0xc1200004,0xc1400003,0x80200009,0x8000000a,0xc1200003,0xc1400003,0x8020000b,0x8000000c,0xc1200003,0xc1400005,0x8020000d,0x8800000e,0x8800000f,0x80000010,0xc1200005},
            (short)0,
            new String[]{"Time","Value",null,"UV","Connector","Value","Average",null,"Light","Connector","Value","Average",null,"Moisture","Connector","Value","Average",null,"Button","Connector","Value",null,"Motion","Connector","Value",null,"Rotary","Connector","Value","Delta","Speed",null},
            new long[]{10, 11, 0, 20, 21, 22, 23, 0, 30, 31, 32, 33, 0, 40, 41, 42, 43, 0, 50, 51, 52, 0, 60, 61, 62, 0, 70, 71, 72, 73, 74, 0},
            new String[]{"global",null,null,"global",null,null,null,null,"global",null,null,null,null,"global",null,null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,null,null,null},
            "GroveResponse.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});

    
    public static final GroveResponseSchema instance = new GroveResponseSchema();
    
    public static final int MSG_TIME_10 = 0x00000000;
    public static final int MSG_TIME_10_FIELD_VALUE_11 = 0x00800001;
    public static final int MSG_UV_20 = 0x00000003;
    public static final int MSG_UV_20_FIELD_CONNECTOR_21 = 0x00000001;
    public static final int MSG_UV_20_FIELD_VALUE_22 = 0x00000002;
    public static final int MSG_UV_20_FIELD_AVERAGE_23 = 0x00000003;
    public static final int MSG_LIGHT_30 = 0x00000008;
    public static final int MSG_LIGHT_30_FIELD_CONNECTOR_31 = 0x00000001;
    public static final int MSG_LIGHT_30_FIELD_VALUE_32 = 0x00000002;
    public static final int MSG_LIGHT_30_FIELD_AVERAGE_33 = 0x00000003;
    public static final int MSG_MOISTURE_40 = 0x0000000D;
    public static final int MSG_MOISTURE_40_FIELD_CONNECTOR_41 = 0x00000001;
    public static final int MSG_MOISTURE_40_FIELD_VALUE_42 = 0x00000002;
    public static final int MSG_MOISTURE_40_FIELD_AVERAGE_43 = 0x00000003;
    public static final int MSG_BUTTON_50 = 0x00000012;
    public static final int MSG_BUTTON_50_FIELD_CONNECTOR_51 = 0x00000001;
    public static final int MSG_BUTTON_50_FIELD_VALUE_52 = 0x00000002;
    public static final int MSG_MOTION_60 = 0x00000016;
    public static final int MSG_MOTION_60_FIELD_CONNECTOR_61 = 0x00000001;
    public static final int MSG_MOTION_60_FIELD_VALUE_62 = 0x00000002;
    public static final int MSG_ROTARY_70 = 0x0000001A;
    public static final int MSG_ROTARY_70_FIELD_CONNECTOR_71 = 0x00000001;
    public static final int MSG_ROTARY_70_FIELD_VALUE_72 = 0x00400002;
    public static final int MSG_ROTARY_70_FIELD_DELTA_73 = 0x00400003;
    public static final int MSG_ROTARY_70_FIELD_SPEED_74 = 0x00000004;

       
    
    private GroveResponseSchema() {
        super(FROM);
    }
        
}
