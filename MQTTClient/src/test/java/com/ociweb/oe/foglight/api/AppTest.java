package com.ociweb.oe.foglight.api;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	//cloud bees has no MQTT server to talk to.
	 @Ignore
	    public void testApp()
	    {
		    FogRuntime runtime = FogRuntime.test(new MQTTClient());	    	
   	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	runtime.getScheduler().startup();
	    	
	    	int iterations = 1;
			while (--iterations >= 0) {
				    		
					runtime.getScheduler().run();
					
					//test application here
					
			}
			
			runtime.getScheduler().shutdown();
			
	    }
}
