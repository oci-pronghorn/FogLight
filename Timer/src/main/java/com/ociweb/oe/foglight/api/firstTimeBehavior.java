package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogRuntime;

public class firstTimeBehavior implements TimeListener {
	private static final long timeInterval = 60_000; //Time in milliseconds

	public firstTimeBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
		
		
	}

	@Override
	public void timeEvent(long time, int iteration) {
		// TODO Auto-generated method stub
		if(time%timeInterval == 0){	
    		System.out.println("clock");
		}
	}

}
