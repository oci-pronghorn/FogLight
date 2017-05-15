package com.ociweb.iot.project.lightblink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

    @Test //TODO need to dtermine.
    //@Ignore
    public void testApp()
    {
    	DeviceRuntime runtime = DeviceRuntime.test(new IoTApp());
    	    	
    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
    
    	scheduler.startup();
    	 
    	TestHardware hardware = (TestHardware)runtime.getHardware();
    
    	int iterations = 4;
    	
    	int expected = 1;
    	
    	long lastTime = 0;
    	while (iterations>0) {
    		
    		scheduler.run();
    		
    		long time = hardware.getLastTime(IoTApp.LED_PORT);
    		if (0!=time) {
    			iterations--;
    			assertEquals(expected, hardware.read(IoTApp.LED_PORT));
    			expected = 1&(expected+1);
    			
    			if (0!=lastTime) {
    				long durationMs = time-lastTime;
    				assertTrue(durationMs>=500);
    				assertTrue(durationMs<=750);    				
    			}
    			
    			lastTime = time;
    			hardware.clearCaputuredLastTimes();
    			hardware.clearCaputuredHighs();
    		}
	    	
    		
    	}
    }
    
}
