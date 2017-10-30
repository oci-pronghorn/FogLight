package com.ociweb.oe.foglight.api;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	
	 @Test
	    public void testApp()
	    {
		 
		 FogRuntime.testUntilShutdownRequested(new Shutdown(), 1000);
		 

			
	    }
}
