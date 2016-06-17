package com.ociweb.device;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.iot.grove.device.lcdrgb.LCDRGBBacklightSchema;
import com.ociweb.iot.grove.device.lcdrgb.LCDRGBContentSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class LCDRGBSchemaValidation {

    
    @Test
    public void groveResponseFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/device/I2C_LCD_RGB_Backlight_Request.xml", LCDRGBBacklightSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(LCDRGBBacklightSchema.instance));
    }
    
    @Test
    public void groveRequestFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/device/I2C_LCD_RGB_Content_Request.xml", LCDRGBContentSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(LCDRGBContentSchema.instance));
    }

    
}
