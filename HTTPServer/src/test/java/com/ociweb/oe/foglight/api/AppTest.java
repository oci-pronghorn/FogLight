package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Test
	public void testApp() {
		boolean cleanExit = FogRuntime.testUntilShutdownRequested(new HTTPServer(), 100);
		assertTrue(cleanExit);
	}
}
