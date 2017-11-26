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
    	
    	HTTPGetBehaviorSingle temp = new HTTPGetBehaviorSingle(runtime);
		runtime.addStartupListener(temp);
			   	
    	
    	runtime.addResponseListener(new HTTPResponse()).includeHTTPSession(session);
    	runtime.addStartupListener(new HTTPGetBehaviorChained(runtime, session));
    	
    	
    }
          
}
