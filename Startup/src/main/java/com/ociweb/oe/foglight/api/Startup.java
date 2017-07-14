package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

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
