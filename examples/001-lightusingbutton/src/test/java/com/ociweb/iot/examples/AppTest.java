package com.ociweb.iot.examples;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	 @Test
	    public void testApp()
	    {
	    	FogRuntime runtime = FogRuntime.test(new IoTApp());
	    	    	
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    }
}
