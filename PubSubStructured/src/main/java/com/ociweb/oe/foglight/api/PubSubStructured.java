package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class PubSubStructured implements FogApp
{
    static int COUNT_DOWN_FIELD = 1;
    static int SENDER_FIELD = 2;

    @Override
    public void declareConnections(Hardware c) {
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        // On startup kick off behavior will send the first message containing the first "topicOne" value
        runtime.addStartupListener(new KickoffBehavior(runtime, "topicOne", 100));
        // DecrementValueBehavior 1 will process "topicOne" and send to "topicTwo"
        runtime.addPubSubListener(new DecrementValueBehavior(runtime, "topicTwo", 1)).addSubscription("topicOne");
        // DecrementValueBehavior 2 will process "topicTwo" and send to "topicOne"
        runtime.addPubSubListener(new DecrementValueBehavior(runtime, "topicOne", 1)).addSubscription("topicTwo");
        // The prcocess loop will end when value reaches 0 and a shutdown command is issued
    }
}
