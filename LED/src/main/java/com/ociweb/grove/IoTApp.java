package com.ociweb.grove;

import static com.ociweb.iot.grove.GroveTwig.*;
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
     
    	final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        //this digital listener will get all the button press and un-press events 
        runtime.addDigitalListener((port, connection, time, value)->{
        	channel1.setValueAndBlock(LED_PORT, value == 1, 200); 
        });
    }
}
