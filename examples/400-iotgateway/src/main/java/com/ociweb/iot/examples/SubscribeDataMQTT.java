package com.ociweb.iot.examples;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.PubSubService;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogRuntime;

public class SubscribeDataMQTT implements StartupListener {

	
	private MqttConnectOptions connOptions;
	private MqttClient client;
	private final Logger logger = LoggerFactory.getLogger(SubscribeDataMQTT.class);
	private final String subscriptionTopic; //  = "#";
	private final String publishTopic;
    private final PubSubService commandChannel;
	private final String clientId;
	private final String serverURI;
	
	//"tcp://localhost:1883", "TestClient"
	public SubscribeDataMQTT(FogRuntime runtime, String subscriptionTopic, String publishTopic, String serverURI, String clientId) {
		
		this.commandChannel = runtime.newCommandChannel().newPubSubService();
				
		this.connOptions = new MqttConnectOptions();
		
		this.subscriptionTopic = subscriptionTopic;
		this.serverURI = serverURI;
		this.clientId = clientId;
		this.publishTopic = publishTopic;
		
	}

	@Override
	public void startup() {
		
		try {
		client = new MqttClient(serverURI, clientId, new MemoryPersistence());
		client.setCallback(new MqttCallback(){
	
				@Override
				public void connectionLost(Throwable cause) {
					logger.warn("connection lost, re-subscribe after timeout");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					startup();//call startup again.
				}
	
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
								
					logger.info("received MQTT message on topic {}",topic);
					
					commandChannel.publishTopic(publishTopic, w-> {
						w.writeUTF(topic);
						w.write(message.getPayload());
					});
					
				}
	
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {					
				}});
			
			client.connect(connOptions);     
			//client.setTimeToWait(-1);
			client.subscribe(subscriptionTopic, 1);
		
		
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
		
	}

}
