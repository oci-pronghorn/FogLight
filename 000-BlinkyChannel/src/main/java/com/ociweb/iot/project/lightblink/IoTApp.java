package com.ociweb.iot.project.lightblink;

import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.PubSubListener;

public class IoTApp implements IoTSetup {
    
    static final int LED_CONNECTION = 5;
           
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connectDigital(LED, LED_CONNECTION);
        c.setTriggerRate(100);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
    	runtime.addTimeListener(new BlinkerBehavior(runtime));
    	        
    }  
}
