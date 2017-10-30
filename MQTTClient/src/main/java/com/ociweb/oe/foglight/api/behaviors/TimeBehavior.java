package com.ociweb.oe.foglight.api.behaviors;

import java.util.Date;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.gl.api.WaitFor;
import com.ociweb.gl.api.Writable;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.ChannelWriter;

public class TimeBehavior implements TimeListener {
	private int droppedCount = 0;
    private final FogCommandChannel cmdChnl;
	private final String publishTopic;

	public TimeBehavior(FogRuntime runtime, String publishTopic) {
		cmdChnl = runtime.newCommandChannel(DYNAMIC_MESSAGING);
		this.publishTopic = publishTopic;
	}

	@Override
	public void timeEvent(long time, int iteration) {
		int i = 1;//iterations
		while (--i>=0) {
			Date d = new Date(System.currentTimeMillis());
			
			// On the timer event create a payload with a string encoded timestamp
			Writable writable = writer -> writer.writeUTF8Text("'MQTT egress body " + d + "'");
					
			// Send out the payload with thre MQTT topic "topic/egress"
			boolean ok = cmdChnl.publishTopic(publishTopic, writable, WaitFor.None);
			if (ok) {
				//System.err.println("sent "+d);
			}
			else {
				droppedCount++;
				System.err.println("The system is backed up, dropped "+droppedCount);
			}
		}
	}
}
