package com.ociweb.iot.trafficlight;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	
	 @Test
	    public void testApp()
	    {
	        DeviceRuntime runtime = DeviceRuntime.test(new IoTApp());
	    	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    

	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	scheduler.startup();
	    	
	    	int i = 14;
	    	while (--i>=0) {
	    		scheduler.run();
	    	}
	    	
	    	scheduler.shutdown();
	    	
	    }
}
