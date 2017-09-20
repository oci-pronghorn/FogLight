package com.ociweb.grove;

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
	    	FogRuntime runtime = FogRuntime.test(new IoTApp());	    	
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
