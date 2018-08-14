package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.AngleSensor;
import static com.ociweb.iot.maker.Port.A0;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

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
