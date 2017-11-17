package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class CountBehavior implements PubSubListener {

	public static int count = 0;
    private final CharSequence publishTopic;
	final FogCommandChannel channel2;
	
	public CountBehavior(FogRuntime runtime, CharSequence publishTopic) {

		channel2 = runtime.newCommandChannel(DYNAMIC_MESSAGING);;
		
		this.publishTopic = publishTopic;
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		count++;
		
		if(count<7){
			return channel2.publishTopic(publishTopic);
		}
		else {
			channel2.shutdown();
		}
		return true;
	}

}
