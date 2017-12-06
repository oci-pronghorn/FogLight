package com.ociweb.oe.foglight.api;


import com.ociweb.gl.api.HTTPSession;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class HTTPClient implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {   
    	c.useNetClient();
    	//c.enableTelemetry();
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {       
    	HTTPSession session = new HTTPSession("www.objectcomputing.com",80,0);
    	
    	session = new HTTPSession("www.objectcomputing.com",80,1);
    	HTTPGetBehaviorSingle temp = new HTTPGetBehaviorSingle(runtime, session);
		runtime.addStartupListener(temp).includeHTTPSession(session);
			   	
    	
    	runtime.addResponseListener(new HTTPResponse()).includeHTTPSession(session);
    	runtime.addStartupListener(new HTTPGetBehaviorChained(runtime, session));
    	
    	
    }
          
}
