package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.LightSensor;
import static com.ociweb.iot.maker.Port.A2;
import static com.ociweb.iot.maker.Port.DIGITALS;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

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
