package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class KickoffBehavior implements StartupListener {
	private final FogCommandChannel cmd;
	private final CharSequence publishTopic;
	private final long countDownFrom;

	KickoffBehavior(FogRuntime runtime, CharSequence publishTopic, long countDownFrom) {
		cmd = runtime.newCommandChannel(DYNAMIC_MESSAGING);
		this.publishTopic = publishTopic;
		this.countDownFrom = countDownFrom;
	}

	@Override
	public void startup() {
		// Send the initial value on startup
		cmd.presumePublishStructuredTopic(publishTopic, writer -> {
			writer.writeUTF8(PubSubStructured.SENDER_FIELD, "from kickoff behavior");
			writer.writeLong(PubSubStructured.COUNT_DOWN_FIELD, countDownFrom);
		});
	}
}
