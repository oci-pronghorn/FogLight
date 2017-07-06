package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.PubSubWriter;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class IngressBehavior implements PubSubListener {

	final FogCommandChannel cmd;
	public IngressBehavior(FogRuntime runtime) {
				
		 cmd = runtime.newCommandChannel(DYNAMIC_MESSAGING);

	}


	public boolean message(CharSequence topic, MessageReader payload) {
		
		System.out.print("\ningress body: ");
		payload.readUTFOfLength(payload.available(), System.out);
		System.out.println();
		
		PubSubWritable writable = new PubSubWritable() {

			@Override
			public void write(PubSubWriter writer) {
				
				writer.writeUTF("second step test message");
			}
			
		};
		cmd.publishTopic("localtest", writable);
		
		return true;
	}

}
