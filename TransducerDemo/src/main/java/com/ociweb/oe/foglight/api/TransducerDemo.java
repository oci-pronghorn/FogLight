package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class TransducerDemo implements FogApp
{
   
	private static final Port LIGHT_SENSOR_PORT = A1;


    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
        c.setTimerPulseRate(2000);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.registerListener(new CustomSumTransducerBehavior());
    }
          
}
