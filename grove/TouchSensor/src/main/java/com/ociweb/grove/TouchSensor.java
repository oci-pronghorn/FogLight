package com.ociweb.grove;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;
import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D4;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class TouchSensor implements FogApp
{
	private static final Port TOUCH_SENSOR_PORT = D4;
	private static final Port LED_PORT = D2;
	
    @Override
    public void declareConnections(Hardware c) {
    	c.connect(TouchSensor, TOUCH_SENSOR_PORT);
    	c.connect(LED, LED_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.addDigitalListener(new TouchSensorBehavior(runtime));
    }
}
