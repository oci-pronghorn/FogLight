package com.ociweb.oe.floglight.api;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	//cloud bees has no MQTT server to talk to.
	 @Ignore
	    public void testApp()
	    {
		    FogRuntime runtime = FogRuntime.test(new MQTTClient());	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	scheduler.startup();
	    	
	    	int iterations = 1;
			while (--iterations >= 0) {
				    		
					scheduler.run();
					
					//test application here
					
			}
			
			scheduler.shutdown();
			
	    }
}
