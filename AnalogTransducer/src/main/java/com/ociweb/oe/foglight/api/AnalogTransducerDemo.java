package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig;

public class AnalogTransducerDemo implements FogApp
{
	private final Port sensorPort = A0;

    @Override
    public void declareConnections(Hardware c) {
      c.connect(AngleSensor, sensorPort);
    
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.registerListener(new AnalogTransducerDemoBehavior(runtime, sensorPort)).includePorts(sensorPort);
    }
}
