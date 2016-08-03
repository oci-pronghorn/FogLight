package com.ociweb.iot.trafficlight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.iot.hardware.TestHardware;
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
	    
	    	scheduler.setSingleStepMode(true);

	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	
	    	//test application here
	    	
	    }
}
