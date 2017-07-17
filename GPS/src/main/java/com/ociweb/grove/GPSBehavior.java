package com.ociweb.grove;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;
import static com.ociweb.iot.grove.gps.GPSTwig.*;

import com.ociweb.iot.grove.gps.GPS_Facade;
import com.ociweb.iot.grove.gps.GeoCoordinateListener;

public class GPSBehavior implements GeoCoordinateListener{
	private FogCommandChannel ch;
	private GPS_Facade gps;
	public GPSBehavior(FogRuntime rt){
		this.gps = GPS.newFacade(rt.newCommandChannel(SERIAL_WRITER));
	}
	@Override
	public void coordinates(int longtitude, int lattitude) {
		// TODO Auto-generated method stub
		
	}
	
}
