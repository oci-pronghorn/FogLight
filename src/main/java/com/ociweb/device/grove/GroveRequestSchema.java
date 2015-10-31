package com.ociweb.device.grove;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveRequestSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc1400003,0x80200000,0x80000001,0xc1200003,0xc1400003,0x80200002,0x80000003,0xc1200003,0xc1400004,0x80200004,0xa4000000,0x84000005,0xc1200004,0xc1400003,0x80200006,0x80000007,0xc1200003,0xc1400003,0x80200008,0xb8000001,0xc1200003},
            (short)0,
            new String[]{"Buzzer","Connector","Duration",null,"Relay","Connector","Duration",null,"LCD","Connector","Text","Color",null,"Servo","Connector","Position",null,"I2C","Addess","Payload",null},
            new long[]{110, 111, 112, 0, 120, 121, 122, 0, 30, 131, 132, 133, 0, 140, 141, 142, 0, 100, 101, 102, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null,null,"global",null,null,null,"global",null,null,null},
            "GroveRequest.xml");
    
    public static final int MSG_BUZZER_110 = 0x0;
    public static final int MSG_BUZZER_110_FIELD_CONNECTOR_111 = 0x1;
    public static final int MSG_BUZZER_110_FIELD_DURATION_112 = 0x2;
    public static final int MSG_RELAY_120 = 0x4;
    public static final int MSG_RELAY_120_FIELD_CONNECTOR_121 = 0x1;
    public static final int MSG_RELAY_120_FIELD_DURATION_122 = 0x2;
    public static final int MSG_LCD_30 = 0x8;
    public static final int MSG_LCD_30_FIELD_CONNECTOR_131 = 0x1;
    public static final int MSG_LCD_30_FIELD_TEXT_132 = 0x4800002;
    public static final int MSG_LCD_30_FIELD_COLOR_133 = 0x800004;
    public static final int MSG_SERVO_140 = 0xd;
    public static final int MSG_SERVO_140_FIELD_CONNECTOR_141 = 0x1;
    public static final int MSG_SERVO_140_FIELD_POSITION_142 = 0x2;
    public static final int MSG_I2C_100 = 0x11;
    public static final int MSG_I2C_100_FIELD_ADDESS_101 = 0x1;
    public static final int MSG_I2C_100_FIELD_PAYLOAD_102 = 0x7000002;
     
    
    public static final GroveRequestSchema instance = new GroveRequestSchema();
    
    private GroveRequestSchema() {
        super(FROM);
    }
        
}
