package com.ociweb.pronghorn.iot.schema;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;
public class GoSchema extends MessageSchema {

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400003,0x80000000,0x80000001,0xc0200003,0xc0400002,0x80000002,0xc0200002},
            (short)0,
            new String[]{"Go","PipeIdx","Count",null,"Release","Count",null},
            new long[]{10, 11, 12, 0, 20, 22, 0},
            new String[]{"global",null,null,null,"global",null,null},
            "GoSchema.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_GO_10 = 0x00000000;
    public static final int MSG_GO_10_FIELD_PIPEIDX_11 = 0x00000001;
    public static final int MSG_GO_10_FIELD_COUNT_12 = 0x00000002;
    public static final int MSG_RELEASE_20 = 0x00000004;
    public static final int MSG_RELEASE_20_FIELD_COUNT_22 = 0x00000001;
    
    public static final GoSchema instance = new GoSchema();
    
    private GoSchema() {
        super(FROM);
    }
        
}
