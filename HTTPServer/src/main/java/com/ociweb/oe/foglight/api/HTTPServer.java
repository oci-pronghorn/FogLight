package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.pronghorn.network.config.HTTPHeaderDefaults;

public class HTTPServer implements FogApp
{
	byte[] cookieHeader = HTTPHeaderDefaults.COOKIE.rootBytes();
	int routeId;
	byte[] myArgName = "myarg".getBytes();
	
    @Override
    public void declareConnections(Hardware c) {
        
		c.enableServer(false, 8088);    	
		routeId = c.registerRoute("/testpage?arg=#{myarg}", cookieHeader);
		
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.addRestListener(new RestBehavior(runtime, myArgName)).includeRoutes(routeId);
    }
          
    
    //TODO: need an example showing the file server
    
    //TODO: need an example showing large file return with continuation.
    
}
