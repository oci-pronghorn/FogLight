package com.ociweb.oe.floglight.api;

import java.util.Date;

import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.gl.api.WaitFor;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobWriter;


public class TimeBehavior implements TimeListener {

	private int droppedCount = 0;
	final FogCommandChannel cmdChnl;
	public TimeBehavior(FogRuntime runtime) {
		cmdChnl = runtime.newCommandChannel(DYNAMIC_MESSAGING);	
	}

	@Override
	public void timeEvent(long time, int iteration) {

		int i = 10;//iterations
		while (--i>=0) {
		
			Date d =new Date(System.currentTimeMillis());
			
			// On the timer event create a payload with a string encoded timestamp
			PubSubWritable writable = new PubSubWritable() {
				@Override
				public void write(BlobWriter writer) {
					writer.writeUTF8Text("egress body "+d);
				}
			};
					
			// Send out the payload with thre MQTT topic "topic/egress"
			boolean ok = cmdChnl.publishTopic("topic/egress", writable, WaitFor.None);
			if (ok) {
				//System.err.println("sent "+d);
			} else {
				droppedCount++;
				System.err.println("The system is backed up, dropped "+droppedCount);
			}
		}
	}

}
