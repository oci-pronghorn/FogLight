package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.transducer.AnalogListenerTransducer;

public class CustomSumTransducer implements AnalogListenerTransducer {
	private final FogRuntime runtime;
	private int sum = 0;
	private int counter = 0;

	CustomSumTransducer(FogRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		
		sum +=value;
		System.out.println("Current sum: " + sum);
		counter++;
		if (counter == 5) {
			runtime.shutdownRuntime();
		}
	}

}
