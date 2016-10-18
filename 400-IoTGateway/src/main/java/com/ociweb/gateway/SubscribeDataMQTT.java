package com.ociweb.gateway;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadWriter;
import com.ociweb.iot.maker.StartupListener;

public class SubscribeDataMQTT implements StartupListener {

	
	private MqttConnectOptions connOptions;
	private MqttClient client;
	private final Logger logger = LoggerFactory.getLogger(SubscribeDataMQTT.class);
	private final String subscriptionTopic; //  = "#";
	private final String publishTopic;
    private final CommandChannel commandChannel;
	private final String clientId;
	private final String serverURI;
	
	//"tcp://localhost:1883", "TestClient"
	public SubscribeDataMQTT(DeviceRuntime runtime, String subscriptionTopic, String publishTopic, String serverURI, String clientId) {
		
		this.commandChannel = runtime.newCommandChannel();
				
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
					
					PayloadWriter payload = commandChannel.openTopic(publishTopic);					
					payload.writeUTF(topic);
					payload.write(message.getPayload());
					payload.publish();
					
				}
	
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {					
				}});
			
			client.connect(connOptions);     
			//client.setTimeToWait(-1);
			client.subscribe(subscriptionTopic, 0);//"/root", 0);
		
		
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
		
	}

}
