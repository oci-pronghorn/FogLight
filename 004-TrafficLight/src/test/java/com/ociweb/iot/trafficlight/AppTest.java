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
		 
		    IoTApp.RED_MS = 1000; //  1 SEC
		    IoTApp.GREEN_MS = 800; // .8 SEC
		    IoTApp.YELLOW_MS = 200; // .2 SEC 
				 
		 
	        DeviceRuntime runtime = DeviceRuntime.test(new IoTApp());
	    	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    

	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	scheduler.startup();
	    	
	    	
	    	long next = System.currentTimeMillis()+5_000;
	    	
	    	while (System.currentTimeMillis() < next) {
	    		scheduler.run();
	    		Thread.yield();
	    	}
	    	
	    	scheduler.shutdown();
	    	
	    }
}
