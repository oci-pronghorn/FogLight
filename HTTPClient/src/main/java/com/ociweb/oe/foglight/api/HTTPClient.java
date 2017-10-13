package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class HTTPClient implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {   
    	c.useNetClient();
    	c.enableTelemetry();
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {       
    	
    	HTTPGetBehaviorSingle temp = new HTTPGetBehaviorSingle(runtime);
		runtime.addStartupListener(temp);
			   	
    	
    	int responseId = runtime.addResponseListener(new HTTPResponse()).getId();    	
    	runtime.addStartupListener(new HTTPGetBehaviorChained(runtime, responseId));
    	
    	
    	
    }
          
}
