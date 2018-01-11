package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	@Ignore
	public void testApp() {
		boolean cleanExit = FogRuntime.testUntilShutdownRequested(new MQTTClient(), 1000);
		assertTrue(cleanExit);
	}
}
