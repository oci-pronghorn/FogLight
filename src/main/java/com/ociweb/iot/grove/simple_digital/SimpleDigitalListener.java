package com.ociweb.iot.grove.simple_digital;
import com.ociweb.iot.maker.Port;


/**
 * 
 * @author Ray Lo
 *
 */
public interface SimpleDigitalListener {
	void simpleDigitalEvent(Port port, long time, long durationMillis, int value);
}
