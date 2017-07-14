package com.ociweb.iot.grove.simple_digital;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;

public class SimpleDigitalFacade implements IODeviceFacade, DigitalListener {
	private final FogCommandChannel ch;
	private final Port p;
	
	private final SimpleDigitalListener dListener;
	
	//TODO: STD DEV LISTENERS AS VARIABLES
	public SimpleDigitalFacade(FogCommandChannel ch, Port p, SimpleDigitalListener dl){
		ch.ensurePinWriting();
		this.ch = ch;
		this.p = p;
		dListener  = dl;
	}
	public boolean setValue(boolean val){
		return ch.setValue(p, val);
	}
	
	public boolean setValueAndBlock(boolean val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}
	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		dListener.SimpleDigitalEvent(port, time, durationMillis, value);
	}
}
