package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.HTTPSession;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.network.config.HTTPContentType;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class HTTPGetBehaviorSingle implements StartupListener, HTTPResponseListener {

	
	private final FogCommandChannel cmd;
	private HTTPSession session = new HTTPSession("www.objectcomputing.com",80,0);
	 
	public HTTPGetBehaviorSingle(FogRuntime runtime) {
		cmd = runtime.newCommandChannel(NET_REQUESTER);
	}

	@Override
	public void startup() {
		cmd.httpGet(session, "/");
	}

	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {
		
		System.out.println(" status:"+reader.statusCode());
		System.out.println("   type:"+reader.contentType());
		
		Payloadable payload = new Payloadable() {
			@Override
			public void read(ChannelReader reader) {
				System.out.println(reader.readUTFOfLength(reader.available()));
			}
		};
		
		reader.openPayloadData( payload );
		
		return true;
	}

}
