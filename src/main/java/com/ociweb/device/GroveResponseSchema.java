package com.ociweb.device;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveResponseSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400002,0x90000000,0xc0200002,0xc1400003,0x80200000,0x80000001,0xc1200003,0xc1400003,0x80200002,0x80000003,0xc1200003,0xc1400003,0x80200004,0x80000005,0xc1200003,0xc1400003,0x80200006,0x80000007,0xc1200003,0xc1400003,0x80200008,0x80000009,0xc1200003,0xc1400005,0x8020000a,0x8800000b,0x8800000c,0x8000000d,0xc1200005,0xc1400004,0x8020000e,0x8000000f,0x88000010,0xc1200004,0xc1400003,0x80200011,0xb8000000,0xc1200003},
            (short)0,
            new String[]{"Time","Value",null,"UV","Connector","Value",null,"Light","Connector","Value",null,"Moisture","Connector","Value",null,"Button","Connector","Value",null,"Motion","Connector","Value",null,"Rotary","Connector","Value","Delta","Speed",null,"TempratureAndHumidity","Connector","Temprature","Humidity",null,"I2C","Addess","Payload",null},
            new long[]{10, 11, 0, 20, 21, 22, 0, 30, 31, 32, 0, 40, 41, 42, 0, 50, 51, 52, 0, 60, 61, 62, 0, 70, 71, 72, 73, 74, 0, 80, 81, 82, 83, 0, 100, 101, 102, 0},
            new String[]{"global",null,null,"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,null,"global",null,null,null,null,null,"global",null,null,null,null,"global",null,null,null},
            "GroveResponse.xml");
    
    public static final GroveResponseSchema instance = new GroveResponseSchema();
    
    public static final int MSG_TIME_10 = 0x0;
    public static final int MSG_TIME_10_FIELD_VALUE_11 = 0x2000001;
    public static final int MSG_UV_20 = 0x3;
    public static final int MSG_UV_20_FIELD_CONNECTOR_21 = 0x1;
    public static final int MSG_UV_20_FIELD_VALUE_22 = 0x2;
    public static final int MSG_LIGHT_30 = 0x7;
    public static final int MSG_LIGHT_30_FIELD_CONNECTOR_31 = 0x1;
    public static final int MSG_LIGHT_30_FIELD_VALUE_32 = 0x2;
    public static final int MSG_MOISTURE_40 = 0xb;
    public static final int MSG_MOISTURE_40_FIELD_CONNECTOR_41 = 0x1;
    public static final int MSG_MOISTURE_40_FIELD_VALUE_42 = 0x2;
    public static final int MSG_BUTTON_50 = 0xf;
    public static final int MSG_BUTTON_50_FIELD_CONNECTOR_51 = 0x1;
    public static final int MSG_BUTTON_50_FIELD_VALUE_52 = 0x2;
    public static final int MSG_MOTION_60 = 0x13;
    public static final int MSG_MOTION_60_FIELD_CONNECTOR_61 = 0x1;
    public static final int MSG_MOTION_60_FIELD_VALUE_62 = 0x2;
    public static final int MSG_ROTARY_70 = 0x17;
    public static final int MSG_ROTARY_70_FIELD_CONNECTOR_71 = 0x1;
    public static final int MSG_ROTARY_70_FIELD_VALUE_72 = 0x1000002;
    public static final int MSG_ROTARY_70_FIELD_DELTA_73 = 0x1000003;
    public static final int MSG_ROTARY_70_FIELD_SPEED_74 = 0x4;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80 = 0x1d;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_CONNECTOR_81 = 0x1;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_TEMPRATURE_82 = 0x2;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_HUMIDITY_83 = 0x1000003;
    public static final int MSG_I2C_100 = 0x22;
    public static final int MSG_I2C_100_FIELD_ADDESS_101 = 0x1;
    public static final int MSG_I2C_100_FIELD_PAYLOAD_102 = 0x7000002;
       
    
    private GroveResponseSchema() {
        super(FROM);
    }
        
}
