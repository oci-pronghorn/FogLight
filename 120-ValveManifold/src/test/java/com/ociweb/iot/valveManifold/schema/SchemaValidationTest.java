package com.ociweb.iot.valveManifold.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class SchemaValidationTest {
		
    @Test
    public void messageClientNetResponseSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/ValveSchema.xml", ValveSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(ValveSchema.instance));
    }

    
}
