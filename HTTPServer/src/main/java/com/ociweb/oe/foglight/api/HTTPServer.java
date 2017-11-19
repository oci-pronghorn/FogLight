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
	int fileServerId;
	
	
	byte[] myArgName = "myarg".getBytes();
	
    @Override
    public void declareConnections(Hardware c) {
        
        c.useHTTP1xServer(8088);;   	
		emptyResponseRouteId = c.registerRoute("/testpageA?arg=#{myarg}", cookieHeader);
		smallResponseRouteId = c.registerRoute("/testpageB");
		largeResponseRouteId = c.registerRoute("/testpageC", cookieHeader);
		fileServerId         = c.registerRoute("/file${path}");
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
        
        //NOTE .includeAllRoutes() can be used to write a behavior taking all routes
        
        //NOTE when using the above no routes need to be registered and if they are
        //     all other routes will return a 404

    }
   
}
