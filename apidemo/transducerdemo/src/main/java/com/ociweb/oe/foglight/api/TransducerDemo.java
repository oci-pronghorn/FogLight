package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.LightSensor;
import static com.ociweb.iot.maker.Port.A1;

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
        
    	runtime.registerListener(new CustomSumTransducerBehavior(runtime));
    }
          
}
