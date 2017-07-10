package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.PubSubWriter;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class IngressBehavior implements PubSubListener {

	final FogCommandChannel cmd;
	public IngressBehavior(FogRuntime runtime) {
				
		 cmd = runtime.newCommandChannel(DYNAMIC_MESSAGING);

	}


	public boolean message(CharSequence topic, MessageReader payload) {

		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.print("\ningress body: ");

		// Read the message payload and output it to System.out
		payload.readUTFOfLength(payload.available(), System.out);
		System.out.println();

		// Create the on-demand mqtt payload writer
		PubSubWritable mqttPayload = new PubSubWritable() {

			@Override
			public void write(BlobWriter writer) {

				writer.writeUTF("second step test message");
			}

		};

		// On the 'localtest' topic publish the mqtt payload
		cmd.publishTopic("localtest", mqttPayload);

		// We consumed the message
		return true;
	}

}
