package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MQTTBridge;
import com.ociweb.gl.impl.MQTTQOS;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class MQTTClient implements FogApp {

	private MQTTBridge mqttConfig;
	
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
		mqttConfig = builder.useMQTT(//"172.16.10.28", 1883, "NathansPC")
				                      "127.0.0.1", 1883, "my name",200) //default of 10 in flight
							.cleanSession(true)	
							.transmissionRetain(true)
							.keepAliveSeconds(10); 

		// Timer rate
		builder.setTimerPulseRate(30); 
		builder.enableTelemetry();
		
				
	}

	@Override
	public void declareBehavior(final FogRuntime runtime) {

		// Subscribe to the mqtt client given "topic/ingress" - produced by mosquitto_pub
		runtime.bridgeSubscription("topic/ingress", "topic/ingress", mqttConfig).setQoS(MQTTQOS.atLeastOnce);
		// Publish to the mqtt client given "topic/egress" - produced by TimeBehavior
		runtime.bridgeTransmission("topic/egress", "topic/egress", mqttConfig).setQoS(MQTTQOS.atLeastOnce);

		// Inject the timer
		runtime.addTimePulseListener(new TimeBehavior(runtime));

		// Inject a listener for "topic/ingress" - produced by mosquitto_pub
		runtime.addPubSubListener(new IngressBehavior(runtime)).addSubscription("topic/ingress");

		// Inject the listener for "localtest" - produced by IngressBehavior on reception of "topic/ingress"
		runtime.addPubSubListener(new EgressBehavior() ).addSubscription("localtest");
	}

}
