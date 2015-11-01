package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GroveRequestSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc1400003,0x80200000,0x80000001,0xc1200003,0xc1400003,0x80200002,0x80000003,0xc1200003,0xc1400003,0x80200004,0x80000005,0xc1200003},
            (short)0,
            new String[]{"Buzzer","Connector","Duration",null,"Relay","Connector","Duration",null,"Servo","Connector","Position",null},
            new long[]{110, 111, 112, 0, 120, 121, 122, 0, 140, 141, 142, 0},
            new String[]{"global",null,null,null,"global",null,null,null,"global",null,null,null},
            "GroveRequest.xml");
    
    public static final int MSG_BUZZER_110 = 0x0;
    public static final int MSG_BUZZER_110_FIELD_CONNECTOR_111 = 0x1;
    public static final int MSG_BUZZER_110_FIELD_DURATION_112 = 0x2;
    public static final int MSG_RELAY_120 = 0x4;
    public static final int MSG_RELAY_120_FIELD_CONNECTOR_121 = 0x1;
    public static final int MSG_RELAY_120_FIELD_DURATION_122 = 0x2;
    public static final int MSG_SERVO_140 = 0x8;
    public static final int MSG_SERVO_140_FIELD_CONNECTOR_141 = 0x1;
    public static final int MSG_SERVO_140_FIELD_POSITION_142 = 0x2;

     
    
    public static final GroveRequestSchema instance = new GroveRequestSchema();
    
    private GroveRequestSchema() {
        super(FROM);
    }
        
}
