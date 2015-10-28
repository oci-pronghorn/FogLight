package com.ociweb.device;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
    
}
