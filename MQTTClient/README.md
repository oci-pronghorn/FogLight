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

```
package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MQTTBridge;
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
		mqttConfig = builder.useMQTT("127.0.0.1", 1883, "my name")
							.cleanSession(true)
							.transmissionOoS(1)
							.subscriptionQoS(1) 
							.keepAliveSeconds(10); 

		// Timer rate
		builder.setTimerPulseRate(1000); 
		builder.enableTelemetry();
				
	}

	@Override
	public void declareBehavior(final FogRuntime runtime) {

		// Subscribe to the mqtt client given "topic/ingress" - produced by mosquitto_pub
		runtime.bridgeSubscription("topic/ingress", mqttConfig); //optional 2 topics, optional transform lambda
		// Publish to the mqtt client given "topic/egress" - produced by TimeBehavior
		runtime.bridgeTransmission("topic/egress", mqttConfig); //optional 2 topics, optional transform lambda

		// Inject the timer
		runtime.addTimePulseListener(new TimeBehavior(runtime));

		// Inject a listener for "topic/ingress" - produced by mosquitto_pub
		runtime.addPubSubListener(new IngressBehavior(runtime)).addSubscription("topic/ingress");

		// Inject the listener for "localtest" - produced by IngressBehavior on reception of "topic/ingress"
		runtime.addPubSubListener(new EgressBehavior() ).addSubscription("localtest");
	}

}

```
Behavior class:

```
package com.ociweb.oe.floglight.api;

import java.util.Date;

import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.PubSubWriter;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobWriter;


public class TimeBehavior implements TimeListener {

	final FogCommandChannel cmdChnl;
	public TimeBehavior(FogRuntime runtime) {
		cmdChnl = runtime.newCommandChannel(DYNAMIC_MESSAGING);	
	}

	@Override
	public void timeEvent(long time, int iteration) {

		// On the timer event create a payload with a string encoded timestamp
		PubSubWritable writable = new PubSubWritable() {
			@Override
			public void write(BlobWriter writer) {	
				Date d =new Date(System.currentTimeMillis());
				
				System.err.println("sent "+d);
				writer.writeUTF8Text("egress body "+d);
				
			}
		};
		// Send out the payload with thre MQTT topic "topic/egress"
		cmdChnl.publishTopic("topic/egress", writable);
	}

}

```
````
package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubWritable;
import com.ociweb.gl.api.PubSubWriter;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class IngressBehavior implements PubSubListener {

	final FogCommandChannel cmd;
	public IngressBehavior(FogRuntime runtime) {
				
		 cmd = runtime.newCommandChannel(DYNAMIC_MESSAGING);

	}


	public boolean message(CharSequence topic,  BlobReader payload) {

		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.print("\ningress body: ");

		// Read the message payload and output it to System.out
		payload.readUTFOfLength(payload.available(), System.out);
		System.out.println();

		// Create the on-demand mqtt payload writer
		PubSubWritable mqttPayload = new PubSubWritable() {

			@Override
			public void write(BlobWriter writer) {

				writer.writeUTF("\nsecond step test message");
			}

		};

		// On the 'localtest' topic publish the mqtt payload
		cmd.publishTopic("localtest", mqttPayload);

		// We consumed the message
		return true;
	}

}

```
```
package com.ociweb.oe.floglight.api;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.pronghorn.pipe.BlobReader;

public class EgressBehavior implements PubSubListener {

	@Override
	public boolean message(CharSequence topic, BlobReader payload) {

		// topic is the MQTT topic
		// payload is the MQTT payload
		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.println("got topic "+topic+" payload "+payload.readUTF()+"\n");
		
		return true;
	}		

}

```
This class is a simple demonstration of MQTT (Message Queue Telemetry Transport). A lightweight messaging protocal, it was inititially designed for constrained devices and low-bandwidth, high-latency or unreliable networks. This demo uses Mosquitto as a message broker, which means that the messages that are published will go through Mosquitto, which will send them to and subsrcibers of the topic. 
