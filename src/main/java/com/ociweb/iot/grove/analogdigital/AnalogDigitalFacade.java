package com.ociweb.iot.grove.analogdigital;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

public class AnalogDigitalFacade implements IODeviceFacade{
	FogCommandChannel ch;
	public AnalogDigitalFacade(FogCommandChannel ch){
		this.ch = ch;
	}
}
