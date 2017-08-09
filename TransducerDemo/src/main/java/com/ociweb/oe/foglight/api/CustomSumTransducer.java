package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.Port;
import com.ociweb.iot.transducer.AnalogListenerTransducer;

public class CustomSumTransducer implements AnalogListenerTransducer {
	private int sum = 0;
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		
			sum +=value;
			System.out.println("Current sum: " + sum);
		
	}

}
