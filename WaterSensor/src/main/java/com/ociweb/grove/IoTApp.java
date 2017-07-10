package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import static com.ociweb.grove.RestfulWaterSensorConstants.*;
import com.ociweb.iot.maker.*;

public class IoTApp implements FogApp
{
	private int webRoute = -1;
	@Override
	public void declareConnections(Hardware c) {
		c.connect(WaterSensor, WATER_SENSOR_PORT);

		c.enableServer(serverIsTLS, 
				serverIsLarge,
				hostIP,
				8088);	

		c.enableTelemetry(true);

		webRoute = c.registerRoute(requestRoute);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.registerListener(new RestfulWaterSensorBehavior(runtime)).includePorts(WATER_SENSOR_PORT);
	}

}
