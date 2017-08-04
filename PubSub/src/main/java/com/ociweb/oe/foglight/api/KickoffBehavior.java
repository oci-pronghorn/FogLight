package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.util.AppendableProxy;

public class KickoffBehavior implements StartupListener{

	final FogCommandChannel channel0;
	final AppendableProxy target;
	
	public KickoffBehavior(FogRuntime runtime, Appendable target) {
		
		this.channel0 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
		this.target = new AppendableProxy(target);
	
	}

	@Override
	public void startup() {
		
		target.append("Your lucky numbers are ...\n");

		channel0.publishTopic("Next");
	}

}
