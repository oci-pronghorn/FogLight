package com.ociweb.oe.floglight.api;

import java.util.Date;

import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.PubSubWriter;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobWriter;


public class TimeBehavior implements TimeListener {

	final FogCommandChannel cmdChnl;
	public TimeBehavior(FogRuntime runtime) {
		cmdChnl = runtime.newCommandChannel(DYNAMIC_MESSAGING);	
	}

	@Override
	public void timeEvent(long time, int iteration) {

		// On the timer event create a payload with a string encoded timestamp
		PubSubWritable writable = new PubSubWritable() {
			@Override
			public void write(BlobWriter writer) {	
				Date d =new Date(System.currentTimeMillis());
				
				System.err.println("sent "+d);
				writer.writeUTF8Text("egress body "+d);
				
			}
		};
		// Send out the payload with thre MQTT topic "topic/egress"
		cmdChnl.publishTopic("topic/egress", writable);
	}

}
