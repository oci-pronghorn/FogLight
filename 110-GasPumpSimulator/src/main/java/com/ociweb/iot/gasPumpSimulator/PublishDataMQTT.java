package com.ociweb.iot.gasPumpSimulator;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;

public class PublishDataMQTT implements PubSubListener{

	private MqttConnectOptions connOptions;
	private MqttClient client;
	private final int QOS = 0;
	private final String serverURI;
	private final String clientId;
	private final String root = "open24";
	private static final Logger logger = LoggerFactory.getLogger(PublishDataMQTT.class);

	public PublishDataMQTT(String serverURI, String clientId) {
		this.connOptions = new MqttConnectOptions();
		this.connOptions.setCleanSession(true);
		this.connOptions.setKeepAliveInterval(0);
		this.connOptions.setConnectionTimeout(0);
		this.serverURI = serverURI;
		this.clientId = clientId;
	}

	@Override
	public void message(CharSequence topic, PayloadReader payload) {

	    try {
		    	if (null==client) {
		    		client = new MqttClient(serverURI, clientId, new MemoryPersistence());
		    	}

	        MqttMessage message = new MqttMessage();

	        int payloadSize = payload.available();
	        byte[] data = new byte[payloadSize];
	        payload.read(data);

	        message.setPayload(data);
	        message.setRetained(false);
	        message.setQos(QOS);

	        client.connect(connOptions);
	        client.setTimeToWait(-1);

	        StringBuilder builder = new StringBuilder();
	        builder.append(root).append('/');
	        builder.append(clientId).append('/');
	        builder.append(topic);

	        client.publish(builder.toString(), message);

	        client.disconnect();

	      } catch (MqttException e) {
	    	  client = null;
	    	  logger.warn("Unable to send payload",e);
	      }
	}

}
