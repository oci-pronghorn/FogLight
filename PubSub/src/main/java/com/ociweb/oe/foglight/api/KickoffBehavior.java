package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class KickoffBehavior implements StartupListener{

	final FogCommandChannel channel0;
	
	public KickoffBehavior(FogRuntime runtime) {
		
		channel0 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
		
	}

	@Override
	public void startup() {
		
		System.out.println("Your lucky numbers are ...");
		
		channel0.publishTopic("Next");
	}

}
