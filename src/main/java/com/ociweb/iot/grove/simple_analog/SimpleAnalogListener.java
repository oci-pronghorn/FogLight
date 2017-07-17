package com.ociweb.iot.grove.simple_analog;

import com.ociweb.iot.maker.Port;

public interface SimpleAnalogListener {
	public void analogEvent(Port port, long time, long durationMillis, int average, int value);
}
