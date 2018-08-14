package com.ociweb.grove;

import static com.ociweb.iot.maker.Port.A1;

import com.ociweb.iot.maker.Port;

public class RestfulWaterSensorConstants {
	protected static final Port WATER_SENSOR_PORT = A1;
	public static final boolean serverIsTLS = false;
	protected static final boolean serverIsLarge = false;
	protected static final String hostIP = "127.0.0.1"; //using localhost as server	
	protected static final String requestRoute = "/water_sensor";
	
	
}
