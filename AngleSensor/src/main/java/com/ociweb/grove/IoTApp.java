package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port LED1_PORT = D3;
    private static final Port LED2_PORT = D4;
    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        
        c.connect(LED, LED1_PORT);
        c.connect(LED,LED2_PORT);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new AngleSensorBehavior(runtime));
    }
}
