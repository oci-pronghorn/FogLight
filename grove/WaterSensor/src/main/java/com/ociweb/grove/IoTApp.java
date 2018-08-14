package com.ociweb.grove;


import static com.ociweb.grove.RestfulWaterSensorConstants.*;
import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.*;

import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.HTTPServerConfig;

public class IoTApp implements FogApp
{
	private int webRoute = -1;
	@Override
	public void declareConnections(Hardware c) {
		c.connect(WaterSensor, WATER_SENSOR_PORT);

    	HTTPServerConfig conf = c.useHTTP1xServer(8088)
    			.setHost(hostIP);
    	if (!serverIsTLS) {
    		conf.useInsecureServer();
    	}

		c.enableTelemetry();

		webRoute = c.defineRoute().path(requestRoute).routeId();
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.registerListener(new RestfulWaterSensorBehavior(runtime)).includePorts(WATER_SENSOR_PORT);
	}

}
