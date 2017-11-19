package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class AnalogListenerBehavior implements AnalogListener {
	private final FogRuntime runtime;

	public AnalogListenerBehavior(FogRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		System.out.println("value: " + value);
		runtime.shutdownRuntime();
	}

}
