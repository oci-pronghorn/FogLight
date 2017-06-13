package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.MotionSensor;
import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup {
           
	private static final Port LED_PORT = D4;
        private static final Port PIR_SENSOR = D3;
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(MotionSensor, PIR_SENSOR);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value==1);
                System.out.println("Stop moving!");                    	        	
        });
              
    }
    
}
