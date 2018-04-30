package com.ociweb.oe.foglight.api;

import java.util.Random;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubService;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.util.Appendables;

public class GenerateBehavior implements PubSubListener {

	Random rand;
    private final CharSequence publishTopic;
	private final Appendable target;
	private final PubSubService pubSubService;
	
	public GenerateBehavior(FogRuntime runtime, CharSequence publishTopic, Appendable target, int seed) {
		
		this.target = target;
		FogCommandChannel channel1 = runtime.newCommandChannel();
		pubSubService = channel1.newPubSubService();

		this.publishTopic = publishTopic;
		this.rand = new Random(seed);
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		int n = rand.nextInt(101);
		Appendables.appendValue(target, "", n, " ");
		
		return pubSubService.publishTopic(publishTopic);
		
	}

}
