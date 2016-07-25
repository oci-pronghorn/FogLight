package com.ociweb.iot.project.lightblink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	    	    	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    }
}
