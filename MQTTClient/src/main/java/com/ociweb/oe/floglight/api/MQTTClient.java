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
		
		mqttConfig = builder.useMQTT("127.0.0.1", 1883, "my name")
							.cleanSession(true)
							.transmissionOoS(2)
							.subscriptionQoS(2) 
							.keepAliveSeconds(10); 
		
		builder.setTriggerRate(1000); 
		//builder.enableTelemetry(true);
				
	}

	@Override
	public void declareBehavior(final FogRuntime runtime) {
				
		runtime.subscriptionBridge("topic/ingress", mqttConfig); //optional 2 topics, optional transform lambda
		runtime.transmissionBridge("topic/egress", mqttConfig); //optional 2 topics, optional transform lambda
			
		runtime.addTimeListener(new TimeBehavior(runtime));
		runtime.addPubSubListener(new IngressBehavior(runtime)).addSubscription("topic/ingress");
		runtime.addPubSubListener(new EgressBehavior() ).addSubscription("localtest");
		
	}

}
