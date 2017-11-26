package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPSession;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class HTTPGetBehaviorChained implements StartupListener {
	
	private FogCommandChannel cmd;
	
    private HTTPSession session;
	
	public HTTPGetBehaviorChained(FogRuntime runtime, HTTPSession session) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.session = session;
	}

	@Override
	public void startup() {
		
		cmd.httpGet(session, "/");
		
	}

}
