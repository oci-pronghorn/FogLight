package com.ociweb.grove;


import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.MotionSensor;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D4;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp {
    
    public static final Port LED_PORT = D4;
    public static final Port PIR_SENSOR = D3;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(MotionSensor, PIR_SENSOR);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new MotionSensorBehavior(runtime));
        
    }
    
}
