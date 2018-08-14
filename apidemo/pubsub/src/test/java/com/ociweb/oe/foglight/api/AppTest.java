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
		StringBuilder target = new StringBuilder();
		boolean cleanExit = FogRuntime.testUntilShutdownRequested(new PubSub(target, 314-579-0066), 100);
		assertTrue(cleanExit);
		//based on seed of 314-579-0066
		assertEquals("Your lucky numbers are ...\n55 57 24 13 69 22 75 ", target.toString());
	}
}
