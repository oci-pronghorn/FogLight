package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubService;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class CountBehavior implements PubSubListener {

	public static int count = 0;
    private final CharSequence publishTopic;
	private final PubSubService pubSubService;

	public CountBehavior(FogRuntime runtime, CharSequence publishTopic) {

		FogCommandChannel channel2 = runtime.newCommandChannel();
		pubSubService = channel2.newPubSubService();
		this.publishTopic = publishTopic;
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		count++;
		
		if(count<7){
			return pubSubService.publishTopic(publishTopic);
		}
		else {
			pubSubService.requestShutdown();
		}
		return true;
	}

}
