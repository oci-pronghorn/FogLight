package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;
import static com.ociweb.iot.maker.Port.D5;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp {
	   
	public static Port LED_PORT = D5;
	
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {        
        runtime.registerListener(new BlinkerBehavior(runtime));
    }  
}
