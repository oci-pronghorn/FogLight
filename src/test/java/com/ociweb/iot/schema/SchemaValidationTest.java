package com.ociweb.iot.schema;

import static org.junit.Assert.assertTrue;

import com.ociweb.pronghorn.iot.schema.*;
import org.junit.Test;

import com.ociweb.pronghorn.pipe.util.build.FROMValidation;
import com.ociweb.pronghorn.schema.ClientNetRequestSchema;
import com.ociweb.pronghorn.schema.ClientNetResponseSchema;
import com.ociweb.pronghorn.schema.MessagePubSub;
import com.ociweb.pronghorn.schema.MessageSubscription;
import com.ociweb.pronghorn.schema.NetParseAckSchema;
import com.ociweb.pronghorn.schema.NetRequestSchema;
import com.ociweb.pronghorn.schema.NetResponseSchema;

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
    public void messageNetParseAckSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/NetParseAck.xml", NetParseAckSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(NetParseAckSchema.instance));
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
    public void piCamSchemaFROMTest() {
        assertTrue(FROMValidation.testForMatchingFROMs("/PiCamSchema.xml", PiCamSchema.instance));
        assertTrue(FROMValidation.testForMatchingLocators(PiCamSchema.instance));
    }
}
