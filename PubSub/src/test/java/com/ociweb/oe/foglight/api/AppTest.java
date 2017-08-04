package com.ociweb.oe.foglight.api;

import static org.junit.Assert.*;

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
		    StringBuilder target = new StringBuilder();
		    FogRuntime runtime = FogRuntime.test(new PubSub(target, 314-579-0066));	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	scheduler.startup();
	    	
	    	int iterations = 100;
			while (--iterations >= 0) {				    		
					scheduler.run();
			}
			
			scheduler.shutdown();
			
			//based on seed of 314-579-0066
			assertEquals("Your lucky numbers are ...\n55 57 24 13 69 22 75 ", target.toString());
			
	    }
}
