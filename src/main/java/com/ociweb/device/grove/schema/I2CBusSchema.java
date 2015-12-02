package com.ociweb.device.grove.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class I2CBusSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x80000000,0x80000001,0x90000000,0xc0200004,0xc0400005,0x80000002,0x80000003,0x80000003,0x90000000,0xc0200005},
            (short)0,
            new String[]{"Point","Clock","Data","Time",null,"State","Task","Step","Byte","Time",null},
            new long[]{100, 101, 102, 103, 0, 200, 201, 202, 202, 103, 0},
            new String[]{"global",null,null,null,null,"global",null,null,null,null,null},
            "I2CBusSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final I2CBusSchema instance = new I2CBusSchema();
    
    private I2CBusSchema() {
        super(FROM);
    }

    public static final int MSG_POINT_100 = 0x00000000;
    public static final int MSG_POINT_100_FIELD_CLOCK_101 = 0x00000001;
    public static final int MSG_POINT_100_FIELD_DATA_102 = 0x00000002;
    public static final int MSG_POINT_100_FIELD_TIME_103 = 0x00800003;
    public static final int MSG_STATE_200 = 0x00000005;
    public static final int MSG_STATE_200_FIELD_TASK_201 = 0x00000001;
    public static final int MSG_STATE_200_FIELD_STEP_202 = 0x00000002;
    public static final int MSG_STATE_200_FIELD_BYTE_202 = 0x00000003;
    public static final int MSG_STATE_200_FIELD_TIME_103 = 0x00800004;

    
    
}
