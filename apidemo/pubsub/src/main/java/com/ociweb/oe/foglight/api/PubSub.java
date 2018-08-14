package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.*;


public class PubSub implements FogApp
{
	private final Appendable target;
	private final int seed;
	
	public PubSub(Appendable target, int seed) {
		this.target = target;
		this.seed = seed;
	}
	
    @Override
    public void declareConnections(Hardware c) {
        //no connections are needed
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addStartupListener(new KickoffBehavior(runtime, target));
    	runtime.addPubSubListener(new GenerateBehavior(runtime, "Count", target, seed)).addSubscription("Next");
    	runtime.addPubSubListener(new CountBehavior(runtime, "Next")).addSubscription("Count");
    	
    	
    }
          
}
