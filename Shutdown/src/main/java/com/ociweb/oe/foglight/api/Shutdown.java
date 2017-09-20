package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;


public class Shutdown implements FogApp
{	
	private static final Port LED_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(Button, BUTTON_PORT);
    	c.connect(LED, LED_PORT);

    }
  
    @Override
    public void declareBehavior(final FogRuntime runtime) {
    	runtime.registerListener(new ShutdownBehavior(runtime)).addSubscription("LED");
    	
    }          

          
}
