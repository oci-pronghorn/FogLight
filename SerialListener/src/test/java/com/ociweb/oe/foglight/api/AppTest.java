package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Test
	public void testApp() {
		StringBuilder builder = new StringBuilder();
		boolean cleanExit = FogRuntime.testUntilShutdownRequested(new SerialListener(builder), 1000);
		assertTrue(cleanExit);
		//TODO: set the TestSerial impl to check the values sent that match [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
		assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9][10, 11, 12, 13, 14, 15, 16, 17, 18, 19]",builder.toString());
	}
}
