package com.ociweb.iot.grove.gps;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.iot.maker.SerialReader;


public class GPS_Facade implements SerialListener{
	private final FogRuntime rt;
	public GPS_Facade(FogRuntime rt){
		this.rt = rt;
	}
	@Override
	public int message(SerialReader reader) {
		
		return 0;
	}
	
}
