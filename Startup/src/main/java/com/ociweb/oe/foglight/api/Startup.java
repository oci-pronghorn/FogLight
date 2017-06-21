package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Startup implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {
    //No connections are needed
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addStartupListener(()->{
    		System.out.println("Hello, this message will display once at start");
    	});
    }
}
