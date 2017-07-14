package com.ociweb.iot.grove.simple_digital;
import com.ociweb.iot.maker.Port;

public interface SimpleDigitalListener {
	void SimpleDigitalEvent(Port port, long time, long durationMillis, int value);
}
