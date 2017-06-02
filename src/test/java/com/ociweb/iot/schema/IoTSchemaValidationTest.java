package com.ociweb.iot.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CBusSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class IoTSchemaValidationTest {

    @Test
    public void uartDataFROMTest() {
        assertTrue(FROMValidation.checkSchema("/UARTDataSchema.xml", SerialDataSchema.class));
    }
    
    @Test
    public void groveResponseFROMTest() {
        assertTrue(FROMValidation.checkSchema("/GroveResponse.xml", GroveResponseSchema.class));
    }
    
    @Test
    public void groveRequestFROMTest() {
        assertTrue(FROMValidation.checkSchema("/GroveRequest.xml", GroveRequestSchema.class));
    }  
    
    @Test
    public void groveI2CBusSchemaFROMTest() {
        assertTrue(FROMValidation.checkSchema("/I2CBusSchema.xml", I2CBusSchema.class));
    }    
    
    @Test
    public void groveI2CCommandSchemaFROMTest() {
        assertTrue(FROMValidation.checkSchema("/I2CCommandSchema.xml", I2CCommandSchema.class));
    } 
    
    @Test
    public void groveI2CResponseSchemaFROMTest() {
        assertTrue(FROMValidation.checkSchema("/I2CResponseSchema.xml", I2CResponseSchema.class));
    } 



    
}
