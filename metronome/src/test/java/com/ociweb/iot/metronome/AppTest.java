package com.ociweb.iot.metronome;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ociweb.iot.hardware.TestHardware;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    
    @BeforeClass
    public static void setup() {
        TestHardware.isInUnitTest = true;       
    }
    
    
    @Test
    public void testApp()
    {
        IOTDeviceRuntime runtime = IOTDeviceRuntime.run(new IoTApp());
                
        NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();      

   //     scheduler.setMinimumStepDurationMS(500);
   //     scheduler.setSingleStepMode(true);

        
        TestHardware hardware = (TestHardware)runtime.getHardware();
        
        //set angle rate value
        
        //record the time of each pulse first and last?
//        
//        scheduler.run();
//        
//        hardware.analogWrite(IoTApp.ROTARY_ANGLE_CONNECTION, 100);        
//     
//        scheduler.run();
        
        
        
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
