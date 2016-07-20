package com.ociweb.iot.metronome;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    
    @Test
    public void testApp()
    {
        IOTDeviceRuntime runtime = IOTDeviceRuntime.test(new IoTApp());
                
        NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();      
        TestHardware hardware = (TestHardware)runtime.getHardware();

        scheduler.setSingleStepMode(true);

        hardware.analogWrite(IoTApp.ROTARY_ANGLE_CONNECTION, 970); //970 will give us 200 BPM and a delay of 300 ms       

        scheduler.startup();
        
        long lastTime = 0;
        int ticks = 10;
        
        hardware.clearCaputuredFirstTimes();
        hardware.clearCaputuredHighs();
        boolean isMetronomeRunning = false;
        
        long startTime = System.currentTimeMillis();
        while (ticks>0 ) {
        	
            scheduler.run();
            
    		long time = hardware.getFirstTime(IoTApp.BUZZER_CONNECTION);
    		if (0!=time) {
    			int high = hardware.getCapturedHigh(IoTApp.BUZZER_CONNECTION);
    			if (0!=high) {
	    			ticks--;
	    			
	    			if (0!=lastTime) {
	    			    if (isMetronomeRunning) {
    	    				long durationMs = (time-lastTime);
    	    				//System.out.println(durationMs+" at "+time);
    	    					    				
    	    				assertTrue(durationMs>=300);
    	    				assertTrue(durationMs<=301);
	    			    } else {
	    			        isMetronomeRunning = true;
	    			    }
	    			}
	    			
	    			lastTime = time;
	    			hardware.clearCaputuredFirstTimes();
	    			hardware.clearCaputuredHighs();
    			} else {
    				//low
    			}
    		}

    		
        }
        
        assertEquals("Did not find all the ticks.",0, ticks);
        
        scheduler.shutdown();
        scheduler.awaitTermination(10, TimeUnit.SECONDS);
        
        
    }
}
