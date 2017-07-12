package com.coiweb.oe.foglight.api;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class AnalogListenerBehavior implements AnalogListener {

	
	public AnalogListenerBehavior(FogRuntime runtime) {
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {

		System.out.println("value: " + value);
	}

}
