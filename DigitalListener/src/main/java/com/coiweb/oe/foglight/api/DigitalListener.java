package com.coiweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class DigitalListener implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
	private static final Port BUZZER_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private static final Port TOUCH_SENSOR_PORT = D4;
	

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
    	c.connect(Buzzer, BUZZER_PORT);
    	c.connect(Button, BUTTON_PORT);
        c.connect(TouchSensor, TOUCH_SENSOR_PORT);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
    	runtime.addDigitalListener(new DigitalListenerBehavior(runtime)).includePorts(DIGITALS);

    }
          
}
