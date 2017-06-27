package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Timer implements FogApp
{
	private static final long timeInterval = 60_000; //Time in milliseconds
	private static long startTime;
	private static boolean haveStartTime = false;

    @Override
    public void declareConnections(Hardware c) {
    	c.setTriggerRate(1); //the rate at which time is checked in milliseconds
        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addTimeListener((time, instance)->{
    		//Demo 1
    		if(time%timeInterval == 0){	
        		System.out.println("clock");
    		}
    		
    		////////////////////////////////////////
    		////////////////////////////////////////
    		
    		//Demo 2
    		//if((time-startTime)%timeInterval == 0){
    		//	System.out.println("clock");
    		//}
    		//if(!haveStartTime){
    		//	startTime = time;
    		//	haveStartTime = true;
    		//}
    	});
    }
}
