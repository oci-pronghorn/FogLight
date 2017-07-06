package com.ociweb.grove;

import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
	private static final Port LED_PORT    = D2;

    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, BUTTON_PORT); 
        c.connect(LED, LED_PORT);        
        c.useI2C();
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
     
        //this digital listener will get all the button press and un-press events 
    	runtime.addDigitalListener(new LEDBehavior(runtime));
    	
    }
}
