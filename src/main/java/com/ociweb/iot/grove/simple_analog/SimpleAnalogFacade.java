package com.ociweb.iot.grove.simple_analog;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;

public class SimpleAnalogFacade implements IODeviceFacade, AnalogListener{
	private FogCommandChannel ch;
	private Port p;
	private final SimpleAnalogListener aListener;
	public SimpleAnalogFacade(FogCommandChannel ch, Port p, SimpleAnalogListener s){
		ch.ensurePinWriting();
		this.p  = p;
		this.ch = ch;
		aListener = s;
	}
	
	public boolean setValue(int val){
		return ch.setValue(p, val);
	}
	
	public boolean setValueAndBlock(int val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		aListener.analogEvent(port, time, durationMillis, average, value);
	}

}
