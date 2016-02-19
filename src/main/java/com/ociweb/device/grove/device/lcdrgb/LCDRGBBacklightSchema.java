package com.ociweb.device.grove.device.lcdrgb;

import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.MessageSchema;

public class LCDRGBBacklightSchema extends MessageSchema{

    public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
            new int[]{0xc0400004,0x80000000,0x80000001,0x80000002,0xc0200004},
            (short)0,
            new String[]{"LCDRGBBackLight","Red","Greed","Blue",null},
            new long[]{200, 201, 202, 203, 0},
            new String[]{"global",null,null,null,null},
            "I2C_LCD_RGB_Backlight_Request.xml",
            new long[]{2, 2, 0},
            new int[]{2, 2, 0});
    
    public static final int MSG_LCDRGBBACKLIGHT_200 = 0x00000000;
    public static final int MSG_LCDRGBBACKLIGHT_200_FIELD_RED_201 = 0x00000001;
    public static final int MSG_LCDRGBBACKLIGHT_200_FIELD_GREED_202 = 0x00000002;
    public static final int MSG_LCDRGBBACKLIGHT_200_FIELD_BLUE_203 = 0x00000003;

    public static final LCDRGBBacklightSchema instance = new LCDRGBBacklightSchema();
    
    private LCDRGBBacklightSchema() {
        super(FROM);
    }
}
