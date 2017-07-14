package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;

public class EgressBehavior implements PubSubListener {

	@Override
	public boolean message(CharSequence topic, MessageReader payload) {

		// topic is the MQTT topic
		// payload is the MQTT payload
		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.println("got topic "+topic+" payload "+payload.readUTF()+"\n");
		
		return true;
	}		

}
