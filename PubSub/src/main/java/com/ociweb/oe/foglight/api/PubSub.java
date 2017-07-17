package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.*;


public class PubSub implements FogApp
{
	
    @Override
    public void declareConnections(Hardware c) {
        //no connections are needed
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addStartupListener(new KickoffBehavior(runtime));
    	runtime.addPubSubListener(new GenerateBehavior(runtime, "Count")).addSubscription("Next");
    	runtime.addPubSubListener(new CountBehavior(runtime, "Next")).addSubscription("Count");
    	
    	
    }
          
}
