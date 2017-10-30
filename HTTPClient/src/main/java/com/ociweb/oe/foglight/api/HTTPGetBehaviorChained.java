package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.HTTPSession;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class HTTPGetBehaviorChained implements StartupListener {
	
	private FogCommandChannel cmd;
	private int responseId;
    private HTTPSession session = new HTTPSession("www.objectcomputing.com",80,0);
	
	public HTTPGetBehaviorChained(FogRuntime runtime, int responseId) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.responseId = responseId;
	}

	@Override
	public void startup() {
		
		cmd.httpGet(session, "/", responseId);
		
	}

}
