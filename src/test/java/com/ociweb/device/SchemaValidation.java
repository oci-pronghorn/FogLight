package com.ociweb.device;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.device.grove.schema.GroveI2CRequestSchema;
import com.ociweb.device.grove.schema.GroveI2CResponseSchema;
import com.ociweb.device.grove.schema.GroveI2CSchema;
import com.ociweb.device.grove.schema.GroveRequestSchema;
import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class SchemaValidation {

    
    @Test
    public void groveResponseFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveResponse.xml", "FROM", GroveResponseSchema.FROM));
    };
    
    @Test
    public void groveResponseFieldsTest() {        
        assertTrue(FROMValidation.testForMatchingLocators(GroveResponseSchema.instance));
    }
    
    @Test
    public void groveRequestFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveRequest.xml", "FROM", GroveRequestSchema.FROM));
    };
    
    @Test
    public void groveRequestFieldsTest() {        
        assertTrue(FROMValidation.testForMatchingLocators(GroveRequestSchema.instance));
    }
    
    @Test
    public void groveI2CFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveI2C.xml", "FROM", GroveI2CSchema.FROM));
    };
    
    @Test
    public void groveI2CFieldsTest() {        
        assertTrue(FROMValidation.testForMatchingLocators(GroveI2CSchema.instance));
    }
    
    @Test
    public void groveI2CRequestFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveI2CRequest.xml", "FROM", GroveI2CRequestSchema.FROM));
    };
    
    @Test
    public void groveI2CRequestFieldsTest() {        
        assertTrue(FROMValidation.testForMatchingLocators(GroveI2CRequestSchema.instance));
    }
    
    @Test
    public void groveI2CResponseFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveI2CResponse.xml", "FROM", GroveI2CResponseSchema.FROM));
    };
    
    @Test
    public void groveI2CResponseFieldsTest() {        
        assertTrue(FROMValidation.testForMatchingLocators(GroveI2CResponseSchema.instance));
    }
    
}
