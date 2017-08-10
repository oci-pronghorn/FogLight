package com.ociweb.oe.floglight.api.behaviors;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.pronghorn.pipe.BlobReader;

public class EgressBehavior implements PubSubListener {

	@Override
	public boolean message(CharSequence topic, BlobReader payload) {
		// topic is the MQTT topic
		// payload is the MQTT payload
		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.println("got topic "+topic+" payload "+payload.readUTF()+"\n");

		return true;
	}
}
