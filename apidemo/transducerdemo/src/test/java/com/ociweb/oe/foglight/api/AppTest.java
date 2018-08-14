package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Test
	@Ignore
	public void testApp() {
		boolean cleanExit = FogRuntime.testConcurrentUntilShutdownRequested(new TransducerDemo(), 1000);
		assertTrue(cleanExit);
	}
}
