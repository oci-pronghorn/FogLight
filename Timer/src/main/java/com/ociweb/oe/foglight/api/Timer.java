package com.ociweb.oe.foglight.api;


//import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Timer implements FogApp
{
	

    @Override
    public void declareConnections(Hardware c) {
    	c.setTimerPulseRate(1); //the rate at which time is checked in milliseconds
        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	
    	runtime.addTimePulseListener(new firstTimeBehavior(runtime));
    	//runtime.addTimeListener(new secondTimeBehavior(runtime));
    	
    }
}
