package com.ociweb.iot.project.lightblink;

import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.PubSubListener;

public class IoTApp implements IoTSetup {
    
    static final int LED_CONNECTION = 5;
           
    public static void main( String[] args) {
        IOTDeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.useConnectD(LED, LED_CONNECTION);
        c.useTriggerRate(100);
    }

    @Override
    public void declareBehavior(IOTDeviceRuntime runtime) {
        
    	runtime.addTimeListener(new BlinkerBehavior(runtime));
    	        
    }  
}
