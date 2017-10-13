# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

-[Mosquitto](https://mosquitto.org/download/), which is an MQTT message broker

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using a MQTT.

Demo code:


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.MQTTBridge;
import com.ociweb.gl.api.MQTTQoS;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.oe.floglight.api.behaviors.EgressBehavior;
import com.ociweb.oe.floglight.api.behaviors.IngressBehavior;
import com.ociweb.oe.floglight.api.behaviors.TimeBehavior;

public class MQTTClient implements FogApp {
	private MQTTBridge mqttConfig;
	
	//install mosquitto - replace 127.0.0.1 if using a different broker
	//
	//to monitor call >    mosquitto_sub -v -t '#' -h 127.0.0.1
	//to test call >       mosquitto_pub -h 127.0.0.1 -t 'external/topic/ingress' -m 'hello'

	@Override
	public void declareConnections(Hardware builder) {
		final String brokerHost = "127.0.0.1";
		//final String brokerHost = "172.16.10.28"; // Nathan's PC
		//final String brokerHost = "thejoveexpress.local"; // Raspberry Pi0
		//final String brokerHost = "badbroker"; // Raspberry Pi0
		// Create a single mqtt client
		mqttConfig = builder.useMQTT(brokerHost, 1883, false, "MQTTClientTest",200) //default of 10 in flight
							.cleanSession(true)	
							.keepAliveSeconds(10);

		// Timer rate
		builder.setTimerPulseRate(300); 
		//builder.enableTelemetry();
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

		final MQTTQoS transQos = MQTTQoS.atLeastOnce;
		final MQTTQoS subscribeQos = MQTTQoS.atLeastOnce;

		// Inject the timer that publishes topic/egress
		TimeBehavior internalEgressTopicProducer = new TimeBehavior(runtime, internalEgressTopic);
		runtime.addTimePulseListener(internalEgressTopicProducer);
		// Convert the internal topic/egress to external for mqtt
		runtime.bridgeTransmission(internalEgressTopic, externalEgressTopic, mqttConfig).setQoS(transQos);
;
		// Subscribe to MQTT topic/ingress (created by mosquitto_pub example in comment above)
		runtime.bridgeSubscription(internalIngressTopic, externalIngressTopic, mqttConfig).setQoS(subscribeQos);
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
```


Behavior class:

#### ERROR:  could not read file ./src/main/java/com/ociweb/oe/foglight/api/TimeBehavior.java

#### ERROR:  could not read file ./src/main/java/com/ociweb/oe/foglight/api/IngressBehavior.java

#### ERROR:  could not read file ./src/main/java/com/ociweb/oe/foglight/api/EgressBehavior.java


This class is a simple demonstration of MQTT (Message Queue Telemetry Transport). A lightweight messaging protocal, it was inititially designed for constrained devices and low-bandwidth, high-latency or unreliable networks. This demo uses Mosquitto as a message broker, which means that the messages that are published will go through Mosquitto, which will send them to and subsrcibers of the topic. 
