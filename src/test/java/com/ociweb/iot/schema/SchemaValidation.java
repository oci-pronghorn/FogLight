package com.ociweb.iot.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CBusSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class SchemaValidation {

    @Test
    public void trafficOrderSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/TrafficOrderSchema.xml", TrafficOrderSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(TrafficOrderSchema.instance));
    }
    
    @Test
    public void trafficReleaseSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/TrafficReleaseSchema.xml", TrafficReleaseSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(TrafficReleaseSchema.instance));
    }
    
    @Test
    public void trafficAckSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/TrafficAckSchema.xml", TrafficAckSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(TrafficAckSchema.instance));
    }
    
    
        
    @Test
    public void groveResponseFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveResponse.xml", GroveResponseSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(GroveResponseSchema.instance));
    }
    
    @Test
    public void groveRequestFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/GroveRequest.xml", GroveRequestSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(GroveRequestSchema.instance));
    }  
    
    @Test
    public void groveI2CBusSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/I2CBusSchema.xml", I2CBusSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(I2CBusSchema.instance));
    }    
    
    @Test
    public void groveI2CCommandSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/I2CCommandSchema.xml", I2CCommandSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(I2CCommandSchema.instance));
    } 
    
    @Test
    public void groveI2CResponseSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/I2CResponseSchema.xml", I2CResponseSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(I2CResponseSchema.instance));
    } 
    
    
}
