package com.ociweb.iot.grove.simple_digital;

import java.util.ArrayList;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;



/**
 * 
 * @author Ray Lo
 *
 */
public class SimpleDigitalTransducer implements IODeviceTransducer, DigitalListener {
	private final FogCommandChannel ch;
	private final Port p;

	//private final SimpleDigitalListener dListener;

	private final ArrayList<SimpleDigitalListener> simpleDigListeners;


	//TODO: STD DEV LISTENERS AS VARIABLES
	public SimpleDigitalTransducer(FogCommandChannel ch, Port p, SimpleDigitalListener... dl){
		ch.ensurePinWriting();
		this.ch = ch;
		this.p = p;
		simpleDigListeners = new ArrayList<SimpleDigitalListener>();

		for (SimpleDigitalListener d: dl){
			simpleDigListeners.add(d);
		}

	}
	public boolean setValue(boolean val){
		return ch.setValue(p, val);
	}

	public boolean setValueAndBlock(boolean val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}
	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		for (SimpleDigitalListener d: simpleDigListeners){
			d.SimpleDigitalEvent(port, time, durationMillis, value);
		}
	}
}
