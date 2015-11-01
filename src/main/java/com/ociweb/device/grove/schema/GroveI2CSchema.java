package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class GroveI2CSchema extends MessageSchema{

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc1400004,0x80200000,0x80000001,0x88000002,0xc1200004,0xc1400003,0x80200003,0xb8000000,0xc1200003},
            (short)0,
            new String[]{"TempratureAndHumidity","Connector","Temprature","Humidity",null,"I2C","Addess","Payload",null},
            new long[]{80, 81, 82, 83, 0, 100, 101, 102, 0},
            new String[]{"global",null,null,null,null,"global",null,null,null},
            "GroveI2C.xml");
    
    //NOT sure this temprature/humdidty stuff really does belong here.
    public static final int MSG_TEMPRATUREANDHUMIDITY_80 = 0x0;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_CONNECTOR_81 = 0x1;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_TEMPRATURE_82 = 0x2;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_HUMIDITY_83 = 0x1000003;
    
    public static final int MSG_I2C_100 = 0x5;
    public static final int MSG_I2C_100_FIELD_ADDESS_101 = 0x1;
    public static final int MSG_I2C_100_FIELD_PAYLOAD_102 = 0x7000002;
    
    public static final GroveI2CSchema instance = new GroveI2CSchema();
    
    private GroveI2CSchema() {
        super(FROM);
    }
}
