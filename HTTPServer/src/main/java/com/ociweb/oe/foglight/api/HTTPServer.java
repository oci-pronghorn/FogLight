package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.pronghorn.network.config.HTTPHeaderDefaults;

public class HTTPServer implements FogApp
{
	byte[] cookieHeader = HTTPHeaderDefaults.COOKIE.rootBytes();
	
	int emptyResponseRouteId;
	int smallResponseRouteId;
	int largeResponseRouteId;
	
	
	byte[] myArgName = "myarg".getBytes();
	
    @Override
    public void declareConnections(Hardware c) {
        
		c.enableServer(false, 8088);    	
		emptyResponseRouteId = c.registerRoute("/testpageA?arg=#{myarg}", cookieHeader);
		smallResponseRouteId = c.registerRoute("/testpageB");
		largeResponseRouteId = c.registerRoute("/testpageC", cookieHeader);
		
		c.enableTelemetry();
		
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.addRestListener(new RestBehaviorEmptyResponse(runtime, myArgName))
                 .includeRoutes(emptyResponseRouteId);
        
        runtime.addRestListener(new RestBehaviorSmallResponse(runtime))
        		.includeRoutes(smallResponseRouteId);
        
        runtime.addRestListener(new RestBehaviorLargeResponse(runtime))
        		 .includeRoutes(largeResponseRouteId);
        
    }
          
    
    //TODO: need an example showing the file server
    
    //TODO: need an example showing large file return with continuation.
    
}
