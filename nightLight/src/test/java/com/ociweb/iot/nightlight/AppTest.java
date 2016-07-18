package com.ociweb.iot.nightlight;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	
	@Test
	public void testApp() {
	    	IOTDeviceRuntime runtime = IOTDeviceRuntime.test(new IoTApp());
	    	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	

	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	
	    	hardware.analogWrite(IoTApp.ANGLE_SENSOR_CONNECTION, 1024);
	    	hardware.analogWrite(IoTApp.LIGHT_SENSOR_CONNECTION, 0);
	    	
	    	scheduler.run();
	    	
	    	//TODO: must ask I2C LCD for its brightness.
	    	
	    	
	    	//test application here
	    	
    }
	 
}
