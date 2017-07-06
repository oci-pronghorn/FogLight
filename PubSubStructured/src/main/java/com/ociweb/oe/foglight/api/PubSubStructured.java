package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class PubSubStructured implements FogApp
{
    public static int COUNT_DOWN_FIELD = 1;
    public static int SENDER_FIELD = 2;


    @Override
    public void declareConnections(Hardware c) {

    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
    	
    	runtime.addStartupListener(new KickoffBehavior(runtime));
    	runtime.addPubSubListener(new ThingBehavior(runtime,"topicOne")).addSubscription("topicTwo");
    	runtime.addPubSubListener(new ThingBehavior(runtime,"topicTwo")).addSubscription("topicOne");

    }
          
}
