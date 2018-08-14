package com.ociweb.grove;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.ScriptedNonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	
	 @Test
	    public void testApp()
	    {
	    	FogRuntime runtime1 = new FogRuntime();
			FogRuntime.test(new IoTApp(), runtime1);
			FogRuntime runtime = runtime1;
	    	ScriptedNonThreadScheduler scheduler = (ScriptedNonThreadScheduler)runtime.getScheduler();
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
