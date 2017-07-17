package com.ociweb.oe.foglight.api;

import java.util.Random;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobReader;

public class GenerateBehavior implements PubSubListener {

	Random rand = new Random();
	private static int count = 0;
    private final CharSequence publishTopic;
	final FogCommandChannel channel1;
	
	public GenerateBehavior(FogRuntime runtime, CharSequence publishTopic) {
		
		channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
		
		this.publishTopic = publishTopic;
	}


	@Override
	public boolean message(CharSequence topic, BlobReader payload) {
		int n = rand.nextInt(101);
		System.out.print(n + " ");

		
		return channel1.publishTopic(publishTopic);
		
	}

}
