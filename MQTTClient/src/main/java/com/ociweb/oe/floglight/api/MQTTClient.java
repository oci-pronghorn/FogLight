package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MQTTBridge;
import com.ociweb.gl.impl.MQTTQOS;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.oe.floglight.api.behaviors.EgressBehavior;
import com.ociweb.oe.floglight.api.behaviors.IngressBehavior;
import com.ociweb.oe.floglight.api.behaviors.TimeBehavior;

public class MQTTClient implements FogApp {
	private MQTTBridge mqttConfig;
	
	//install mosquitto
	//
	//to monitor call >    mosquitto_sub -v -t '#' -h 127.0.0.1
	//to test call >       mosquitto_pub -h 127.0.0.1 -t 'external/topic/ingress' -m 'hello'

	@Override
	public void declareConnections(Hardware builder) {
		// Create a single mqtt client
		mqttConfig = builder.useMQTT(//"172.16.10.28", 1883, "NathansPC")
				                      "127.0.0.1", 1883, "my name",200) //default of 10 in flight
							.cleanSession(true)	
							.keepAliveSeconds(10);

		// Timer rate
		builder.setTimerPulseRate(300); 
		builder.enableTelemetry();
	}

	@Override
	public void declareBehavior(final FogRuntime runtime) {
		// The external/internal topic translation is not necessary.
		// The bridge calls may be made with one topic specified
		final String internalEgressTopic = "internal/topic/egress";
		final String externalEgressTopic = "external/topic/egress";
		final String internalIngressTopic = "internal/topic/ingress";
		final String externalIngressTopic = "external/topic/ingress";
		final String localTestTopic = "localtest";

		// Inject the timer that publishes topic/egress
		TimeBehavior internalEgressTopicProducer = new TimeBehavior(runtime, internalEgressTopic);
		runtime.addTimePulseListener(internalEgressTopicProducer);
		// Convert the internal topic/egress to external for mqtt
		runtime.bridgeTransmission(internalEgressTopic, externalEgressTopic, mqttConfig).setQoS(MQTTQOS.atLeastOnce);
;
		// Subscribe to MQTT topic/ingress (created by mosquitto_pub example in comment above)
		runtime.bridgeSubscription(internalIngressTopic, externalIngressTopic, mqttConfig).setQoS(MQTTQOS.atLeastOnce);
		// Listen to internal/topic/ingress and publish localtest
		IngressBehavior mqttBrokerListener = new IngressBehavior(runtime, localTestTopic);
		runtime.registerListener(mqttBrokerListener)
				.addSubscription(internalIngressTopic, mqttBrokerListener::receiveMqttMessage);

		// Inject the listener for "localtest"
		EgressBehavior doTheBusiness = new EgressBehavior();
		runtime.registerListener(doTheBusiness)
				.addSubscription(localTestTopic, doTheBusiness::receiveTestTopic);
	}
}
