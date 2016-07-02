package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveResponseSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x80000000,0x90000000,0x80000001,0xc0200004,0xc0400005,0x80000002,0x90000000,0x80000003,0x80000004,0xc0200005,0xc0400006,0x80000005,0x90000000,0x88000006,0x88000007,0x80000008,0xc0200006},
            (short)0,
            new String[]{"DigitalSample","Connector","Time","Value",null,"AnalogSample","Connector","Time","Value","Average",null,"Encoder","Connector","Time","Value","Delta","Speed",null},
            new long[]{20, 21, 11, 22, 0, 30, 31, 11, 32, 33, 0, 70, 71, 11, 72, 73, 74, 0},
            new String[]{"global",null,null,null,null,"global",null,null,null,null,null,"global",null,null,null,null,null,null},
            "GroveResponse.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});

    
    public static final GroveResponseSchema instance = new GroveResponseSchema();
    
    public static final int MSG_DIGITALSAMPLE_20 = 0x00000000;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21 = 0x00000001;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_DIGITALSAMPLE_20_FIELD_VALUE_22 = 0x00000004;  
    
    //TODO: add duration that this was in the previous state, cheap to compute and very helpful data, may want to add to all field types, time since last update as a general feature !!!
    
    public static final int MSG_ANALOGSAMPLE_30 = 0x00000005;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31 = 0x00000001;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_VALUE_32 = 0x00000004;
    public static final int MSG_ANALOGSAMPLE_30_FIELD_AVERAGE_33 = 0x00000005;
    public static final int MSG_ENCODER_70 = 0x0000000B;
    public static final int MSG_ENCODER_70_FIELD_CONNECTOR_71 = 0x00000001;
    public static final int MSG_ENCODER_70_FIELD_TIME_11 = 0x00800002;
    public static final int MSG_ENCODER_70_FIELD_VALUE_72 = 0x00400004;
    public static final int MSG_ENCODER_70_FIELD_DELTA_73 = 0x00400005;
    public static final int MSG_ENCODER_70_FIELD_SPEED_74 = 0x00000006;
    
    private GroveResponseSchema() {
        super(FROM);
    }
        
}
