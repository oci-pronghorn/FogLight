package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class GroveI2CResponseSchema extends MessageSchema{

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc1400004,0x80200000,0x80000001,0x88000002,0xc1200004},
            (short)0,
            new String[]{"TempratureAndHumidity","Connector","Temprature","Humidity",null},
            new long[]{80, 81, 82, 83, 0},
            new String[]{"global",null,null,null,null},
            "GroveI2CResponse.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    //NOT sure this temprature/humdidty stuff really does belong here.
    public static final int MSG_TEMPRATUREANDHUMIDITY_80 = 0x00000000;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_CONNECTOR_81 = 0x00000001;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_TEMPRATURE_82 = 0x00000002;
    public static final int MSG_TEMPRATUREANDHUMIDITY_80_FIELD_HUMIDITY_83 = 0x00400003;
    
    
    public static final GroveI2CResponseSchema instance = new GroveI2CResponseSchema();
    
    private GroveI2CResponseSchema() {
        super(FROM);
    }
}
