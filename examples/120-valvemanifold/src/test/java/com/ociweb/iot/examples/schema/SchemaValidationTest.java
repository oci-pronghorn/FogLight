package com.ociweb.iot.examples.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class SchemaValidationTest {
		
    @Test
    public void messageClientNetResponseSchemaFROMTest() {
        assertTrue(FROMValidation.checkSchema("/ValveSchema.xml", ValveSchema.class));
    }

    
}
