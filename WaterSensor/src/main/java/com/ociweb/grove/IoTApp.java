package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;

public class IoTApp implements IoTSetup
{
	private boolean isReportingToWeb = true;
	private int webRoute = -1;


	
	@Override
	public void declareConnections(Hardware c) {
		c.connect(WaterSensor,RestfulWaterSensorConstants.WATER_SENSOR_PORT);

		if (isReportingToWeb){
			c.enableServer(RestfulWaterSensorConstants.serverIsTLS, 
							RestfulWaterSensorConstants.serverIsLarge,
							RestfulWaterSensorConstants.hostIP,
							8088);	
			
			c.enableTelemetry(true);
			
			webRoute = c.registerRoute(RestfulWaterSensorConstants.requestRoute);
		}
	}


	@Override
	public void declareBehavior(DeviceRuntime runtime) {
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.addRestListener(new RestfulWaterSensorBehavior(runtime), webRoute);
	}

}
