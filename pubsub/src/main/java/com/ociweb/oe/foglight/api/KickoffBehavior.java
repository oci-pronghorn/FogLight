package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.PubSubService;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.util.AppendableProxy;

public class KickoffBehavior implements StartupListener{

	private final PubSubService pubSubService;
	final AppendableProxy target;
	
	public KickoffBehavior(FogRuntime runtime, Appendable target) {

		FogCommandChannel channel0 = runtime.newCommandChannel();
		pubSubService = channel0.newPubSubService();
		this.target = new AppendableProxy(target);
	
	}

	@Override
	public void startup() {
		
		target.append("Your lucky numbers are ...\n");

		pubSubService.publishTopic("Next");
	}

}
