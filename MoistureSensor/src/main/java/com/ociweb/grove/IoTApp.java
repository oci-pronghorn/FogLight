package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {
    
    private static final Port SENSOR_PORT = A2;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(MoistureSensor, SENSOR_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.addAnalogListener(new MoistureSensorBehavior(runtime));   
    }
}