package com.ociweb.oe.foglight.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	
	 //@Test
	@Ignore
	    public void testApp()
	    {
		    FogRuntime runtime = FogRuntime.test(new HTTPClient());	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	scheduler.startup();
	    	
	    	int iterations = 10;
			while (--iterations >= 0) {
				    		
					scheduler.run();
					
					//test application here
					
			}
			
			scheduler.shutdown();
			
	    }
}
