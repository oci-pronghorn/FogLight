package com.ociweb.device.grove.device.lcdrgb;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class LCDRGBContentSchema extends MessageSchema{

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400002,0xa0000000,0xc0200002},
            (short)0,
            new String[]{"LCDRGBText","text",null},
            new long[]{210, 211, 0},
            new String[]{"global",null,null},
            "I2C_LCD_RGB_Content_Request.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_LCDRGBTEXT_210 = 0x00000000;
    public static final int MSG_LCDRGBTEXT_210_FIELD_TEXT_211 = 0x01000001;
    
    
    public static final LCDRGBContentSchema instance = new LCDRGBContentSchema();
    
    private LCDRGBContentSchema() {
        super(FROM);
    }
}
