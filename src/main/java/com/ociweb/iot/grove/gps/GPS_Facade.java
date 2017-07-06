package com.ociweb.iot.grove.gps;

import com.ociweb.iot.maker.FogCommandChannel;

public class GPS_Facade {
	private final FogCommandChannel ch;
	public GPS_Facade(FogCommandChannel ch){
		this.ch = ch;
	}
}
