package com.ociweb.iot.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.pronghorn.iot.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.iot.schema.ClientNetResponseSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CBusSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.NetResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class SchemaValidationTest {

    @Test
    public void messageClientNetRequestSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/ClientNetRequest.xml", ClientNetRequestSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(ClientNetRequestSchema.instance));
    }
	
    @Test
    public void messageClientNetResponseSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/ClientNetResponse.xml", ClientNetResponseSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(ClientNetResponseSchema.instance));
    }
    
    @Test
    public void messageNetRequestSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/NetRequest.xml", NetRequestSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(NetRequestSchema.instance));
    }
	
    @Test
    public void messageNetResponseSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/NetResponse.xml", NetResponseSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(NetResponseSchema.instance));
    }
	
    @Test
    public void messagePubSubSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/MessagePubSub.xml", MessagePubSub.instance));
        assertTrue(FROMValidation.testForMatchingLocators(MessagePubSub.instance));
    }
    
    @Test
    public void messageSubscriberSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/MessageSubscriber.xml", MessageSubscription.instance));
        assertTrue(FROMValidation.testForMatchingLocators(MessageSubscription.instance));
    }
    
    
    
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
