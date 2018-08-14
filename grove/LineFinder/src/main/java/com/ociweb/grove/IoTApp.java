package com.ociweb.grove;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LineFinder;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D4;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;



public class IoTApp implements FogApp {
    
    public static final Port LED_PORT = D4;
    public static final Port LINEFINDER_PORT = D3;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LINEFINDER_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new LineFinderBehavior(runtime));
        
    }
    
}