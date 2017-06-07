package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;
public class IoTApp implements IoTSetup
{
	///////////////////////
	//Connection constants 
	///////////////////////
	// // by using constants such as these you can easily use the right value to reference where the sensor was plugged in

	private boolean isReportingToWeb = true;
	private int webRoute = -1;


	
	@Override
	public void declareConnections(Hardware c) {
		////////////////////////////
		//Connection specifications
		///////////////////////////

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
		//////////////////////////////
		//Specify the desired behavior
		//////////////////////////////
		
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.addRestListener(new RestfulWaterSensorBehavior(runtime), webRoute);
	}

}
