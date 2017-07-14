package com.ociweb.iot.grove.analog;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;

public class AnalogFacade implements IODeviceFacade{
	private FogCommandChannel ch;
	private Port p;
	public AnalogFacade(FogCommandChannel ch, Port p){
		ch.ensurePinWriting();
		this.p  = p;
		this.ch = ch;
	}
	
	public boolean setValue(int val){
		return ch.setValue(p, val);
	}
	
	public boolean setValueAndBlock(int val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}

}
