package com.ociweb.iot.metronome;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    
    @Test
    public void testApp()
    {
        DeviceRuntime runtime = DeviceRuntime.test(new IoTApp());
                
        NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();      
        TestHardware hardware = (TestHardware)runtime.getHardware();

        scheduler.setSingleStepMode(true);
        scheduler.setMinimumStepDurationMS(0);
        
        hardware.clearI2CWriteCount();

        hardware.analogWrite(IoTApp.ROTARY_ANGLE_CONNECTION, 970); //970 will give us 200 BPM and a delay of 300 ms       

     
        scheduler.startup();
        
        long lastTime = 0;
        int ticks = 5;
        
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
    	    								
    	    				//due to assertions and garbage when unit tests are run we can not be so strict here
    	    				int overheadForTesting = 10;
    	    				
    	    				int window = 300;
    	    				assertTrue(durationMs+" at "+time, durationMs>=(window-overheadForTesting));    	    				
    	    				assertTrue(durationMs+" at "+time, durationMs<=(window+overheadForTesting));
    	    				
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

        
        int count = hardware.getI2CWriteCount();
        System.out.println(count);
        int c = count;
        while (c>0) {
            hardware.outputLastI2CWrite(System.out, c--).append("\n");
        }
        
        assertEquals("Did not find all the ticks.",0, ticks);
        
        scheduler.shutdown();
        scheduler.awaitTermination(10, TimeUnit.SECONDS);
        
        
    }
}
