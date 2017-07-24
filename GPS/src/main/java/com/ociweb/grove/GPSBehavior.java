package com.ociweb.grove;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;
import static com.ociweb.iot.grove.gps.GPSTwig.*;

import com.ociweb.iot.grove.gps.GPS_Transducer;
import com.ociweb.iot.grove.gps.GeoCoordinateListener;

public class GPSBehavior implements GeoCoordinateListener{
	private FogCommandChannel ch;
	private GPS_Transducer gps;
	public GPSBehavior(FogRuntime rt){
		this.gps = GPS.newTransducer(rt.newCommandChannel(SERIAL_WRITER | I2C_WRITER));
		rt.registerListener(null);
	}
	@Override
	public void coordinates(int longtitude, int lattitude) {
		
	}
	
}
