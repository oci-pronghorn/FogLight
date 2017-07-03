package com.ociweb.grove;

import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

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
    	final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        runtime.addDigitalListener((port, connection, time, value)->{ 
            channel1.setValueAndBlock(LED_PORT, value == 1, 500);                                                                            //delays a future action
        });
    }
}
