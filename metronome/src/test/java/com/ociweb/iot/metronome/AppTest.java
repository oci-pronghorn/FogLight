package com.ociweb.iot.metronome;

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

        scheduler.setSingleStepMode(true);

        
        TestHardware hardware = (TestHardware)runtime.getHardware();
        
        //set angle rate value
        
        //record the time of each pulse first and last?
        
        scheduler.run();

        hardware.digitalWrite(IoTApp.BUTTON_CONNECTION, 1);
        hardware.analogWrite(IoTApp.ROTARY_ANGLE_CONNECTION, 900);        
     
        scheduler.run();
        
    //    hardware.digitalWrite(IoTApp.BUTTON_CONNECTION, 0);
        
        
//        int iterations = 10;
//        
//        int i = iterations;
//        while (--i>=0) {
//            scheduler.run();
//           
//       //     scheduler.run();            
//                      
//        }
    }
}
