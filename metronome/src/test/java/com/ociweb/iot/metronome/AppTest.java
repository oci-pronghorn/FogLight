package com.ociweb.iot.metronome;

import static org.junit.Assert.assertTrue;

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

        int ticks = 1;
        
        while (ticks>0) {
        	
            scheduler.run();
            
    		long time = hardware.getFirstTime(IoTApp.BUZZER_CONNECTION);
    		if (0!=time) {
    			int high = hardware.getCapturedHigh(IoTApp.BUZZER_CONNECTION);
    			if (0!=high) {
	    			ticks--;
	    			
	    			System.out.println("Tick:"+time);
	    			
	    			if (0!=lastTime) {
	    				long durationMs = (time-lastTime);
	    				System.out.println(durationMs);
	    				
	    				assertTrue(durationMs>=300);
	    				assertTrue(durationMs<=301);
	    				
	    			}
	    			
	    			lastTime = time;
	    			hardware.clearCaputuredFirstTimes();
	    			hardware.clearCaputuredHighs();
    			} else {
    				//low
    			}
    		}
            
            
        }
        
    }
}
