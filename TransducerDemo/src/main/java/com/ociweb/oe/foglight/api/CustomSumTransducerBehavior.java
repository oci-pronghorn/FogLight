package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.A1;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.simple_analog.SimpleAnalogListener;
import com.ociweb.iot.grove.simple_analog.SimpleAnalogTransducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class CustomSumTransducerBehavior implements Behavior {
	
	
	CustomSumTransducer cst;
	public CustomSumTransducerBehavior() {
		cst = new CustomSumTransducer();
	}



}
