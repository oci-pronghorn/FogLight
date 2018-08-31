package com.ociweb.iot.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import com.ociweb.iot.hardware.impl.test.BasicTestPortReader;
import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.ScriptedNonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    
    @Test
    public void testApp()
    {
    	
    	
        FogRuntime runtime = FogRuntime.test(new IoTApp());
        
       

        ScriptedNonThreadScheduler scheduler = (ScriptedNonThreadScheduler)runtime.getScheduler();
        TestHardware hardware = (TestHardware)runtime.getHardware();
        hardware.portReader = new BasicTestPortReader();

        hardware.clearI2CWriteCount();

        hardware.write(IoTApp.ROTARY_ANGLE_PORT, 970); //970 will give us 200 BPM and a delay of 300 ms       

     
        scheduler.startup();
        
        long lastTime = 0;
        int ticks = 5;
        
        hardware.clearCaputuredFirstTimes();
        hardware.clearCaputuredHighs();
        boolean isMetronomeRunning = false;
    
        
        final long startTime = System.currentTimeMillis();
        while (ticks>0 /*&& (System.currentTimeMillis()-startTime<40_000)*/ ) {
        	
            scheduler.run();
                        
    		long time = hardware.getFirstTime(IoTApp.BUZZER_PORT);
    		if (0!=time) {
    			int high = hardware.getCapturedHigh(IoTApp.BUZZER_PORT);
    			if (0!=high) {
	    			ticks--;
	    			
	    			if (0!=lastTime) {
	    			    if (isMetronomeRunning) {
    	    				long durationMs = (time-lastTime);
    	    						    	    				
    	    				//due to assertions and garbage when unit tests are run we can not be so strict here
    	    				int overheadForTesting = 10;
    	    				
    	    				int window = 300;
    	    				
    	    				//this is a little too quick now... and under the threshold.
    	    				assertTrue(durationMs+" at "+time, durationMs>=(window-overheadForTesting));    	    				
    	    				assertTrue(durationMs+" at "+time, durationMs<=(window*4));
    	    				
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
