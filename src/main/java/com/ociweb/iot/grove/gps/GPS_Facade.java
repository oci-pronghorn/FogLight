package com.ociweb.iot.grove.gps;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.iot.maker.SerialReader;


public class GPS_Facade implements SerialListener, IODeviceFacade{
	private final FogCommandChannel ch;
	private GeoCoordinateListener l;
	public GPS_Facade(FogCommandChannel ch){
		this.ch = ch;
	}
	@Override
	public int message(SerialReader reader) {
		
		l.coordinates(1, 2);
		return 0;
	}
	
}
