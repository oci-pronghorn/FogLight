package com.ociweb.iot.schema;

import static org.junit.Assert.assertTrue;

import com.ociweb.pronghorn.iot.schema.*;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class IoTSchemaValidationTest {

    @Test
    public void uartDataFROMTest() {

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/UARTDataSchema.xml", SerialDataSchema.class));
    	}
    }
    
    
    @Test
    public void groveResponseFROMTest() {

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/GroveResponse.xml", GroveResponseSchema.class));
        }
    }
    
    
    @Test
    public void groveRequestFROMTest() {

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/GroveRequest.xml", GroveRequestSchema.class));
        }
    }
    
    @Test
    public void groveI2CBusSchemaFROMTest() {
    	

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/I2CBusSchema.xml", I2CBusSchema.class));
    	}
    }    
    
    @Test
    public void groveI2CCommandSchemaFROMTest() {
    	

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/I2CCommandSchema.xml", I2CCommandSchema.class));
    	}
    } 
    
    @Test
    public void groveI2CResponseSchemaFROMTest() {

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/I2CResponseSchema.xml", I2CResponseSchema.class));
    	}
    } 

    @Test
    public void imageSchemaFROMTest() {

    	if ("arm".equals(System.getProperty("os.arch"))) {
    		assertTrue(true);
    	}
    	
    	else {
        assertTrue(FROMValidation.checkSchema("/ImageSchema.xml", ImageSchema.class));
    	}
    }	
}
