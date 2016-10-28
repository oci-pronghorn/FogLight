package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class PiCamSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0xb8000000,0xa0000001,0xc0200003},
            (short)0,
            new String[]{"PiCamPublication","Image","Extension",null},
            new long[]{1, 2, 4, 0},
            new String[]{"global",null,null,null},
            "PiCamSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});

    public static final PiCamSchema instance = new PiCamSchema(FROM);

    public static final int MSG_PICAMPUBLICATION_1 = 0x00000000;
    public static final int MSG_PICAMPUBLICATION_1_FIELD_IMAGE_2 = 0x01c00001;
    public static final int MSG_PICAMPUBLICATION_1_FIELD_EXTENSION_4 = 0x01000003;

    protected PiCamSchema(FieldReferenceOffsetManager from) {
        super(from);
    }
}
