package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;

public class EgressBehavior implements PubSubListener {

	@Override
	public boolean message(CharSequence topic, MessageReader payload) {
		
		System.out.println("got topic "+topic+" payload "+payload.readUTF());
		
		return true;
	}		

}
