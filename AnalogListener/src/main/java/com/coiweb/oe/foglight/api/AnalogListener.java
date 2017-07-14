package com.coiweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class AnalogListener implements FogApp
{
	private static Port LIGHT_SENSOR_PORT = A2;

    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.addAnalogListener(new AnalogListenerBehavior(runtime)).includePorts(LIGHT_SENSOR_PORT).excludePorts(DIGITALS);
    }
          
}
