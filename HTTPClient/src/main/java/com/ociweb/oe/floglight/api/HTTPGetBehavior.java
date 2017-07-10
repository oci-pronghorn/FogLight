package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class HTTPGetBehavior implements StartupListener {

	
	private FogCommandChannel cmd;
	private int responseId;

	public HTTPGetBehavior(FogRuntime runtime, int responseId) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.responseId = responseId;
	}

	@Override
	public void startup() {
		
		//TODO: how do we support TLS or not?
		cmd.httpGet("google.com", 443, "/", responseId);
		
	}

}
