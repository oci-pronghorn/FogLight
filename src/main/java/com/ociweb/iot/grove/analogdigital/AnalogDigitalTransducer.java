package com.ociweb.iot.grove.analogdigital;

import com.ociweb.iot.maker.FogCommandChannel;

import com.ociweb.iot.maker.IODeviceTransducer;

public class AnalogDigitalTransducer implements IODeviceTransducer{
	FogCommandChannel ch;
	public AnalogDigitalTransducer(FogCommandChannel ch){
		this.ch = ch;
	}
}
