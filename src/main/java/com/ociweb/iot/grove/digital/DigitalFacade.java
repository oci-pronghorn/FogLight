package com.ociweb.iot.grove.digital;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;

public class DigitalFacade implements IODeviceFacade {
	private FogCommandChannel ch;
	private Port p;
	public DigitalFacade(FogCommandChannel ch, Port p){
		ch.ensurePinWriting();
		this.ch = ch;
		this.p = p;
	}
	public boolean setValue(boolean val){
		return ch.setValue(p, val);
	}
	
	public boolean setValueAndBlock(boolean val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}
}
