package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MQTTConfig;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class MQTTClient implements FogApp {

	private MQTTConfig mqttConfig;
	
	//install mosquitto
	//
	//to monitor call >    mosquitto_sub -v -t '#' -h 127.0.0.1
	//to test call >       mosquitto_pub -h 127.0.0.1 -t 'topic/ingress' -m 'hello'
	
	public static void main( String[] args ) {
		FogRuntime.run(new MQTTClient());
    }
		
	@Override
	public void declareConnections(Hardware builder) {

		// Create a single mqtt client
		mqttConfig = builder.useMQTT("127.0.0.1", 1883, "my name")
							.cleanSession(true)
							.transmissionOoS(2)
							.subscriptionQoS(2) 
							.keepAliveSeconds(10); 

		// Timer rate
		builder.setTriggerRate(1000); 
		//builder.enableTelemetry(true);
				
	}

	@Override
	public void declareBehavior(final FogRuntime runtime) {

		// Subscribe to the mqtt client given "topic/ingress" - produced by mosquitto_pub
		runtime.subscriptionBridge("topic/ingress", mqttConfig); //optional 2 topics, optional transform lambda
		// Publish to the mqtt client given "topic/egress" - produced by TimeBehavior
		runtime.transmissionBridge("topic/egress", mqttConfig); //optional 2 topics, optional transform lambda

		// Inject the timer
		runtime.addTimeListener(new TimeBehavior(runtime));

		// Inject a listener for "topic/ingress" - produced by mosquitto_pub
		runtime.addPubSubListener(new IngressBehavior(runtime)).addSubscription("topic/ingress");

		// Inject the listener for "localtest" - produced by IngressBehavior on reception of "topic/ingress"
		runtime.addPubSubListener(new EgressBehavior() ).addSubscription("localtest");
	}

}
