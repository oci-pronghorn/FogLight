package com.ociweb.gateway;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;

public class PublishKafka implements PubSubListener {

	private final Properties properties=new Properties();
    private final String publishTopic;
	
	public PublishKafka(DeviceRuntime runtime, String publishTopic) {
		
		properties.setProperty("bootstrap.servers",    "localhost:9092");
		properties.setProperty("acks",                 "all");
		properties.setProperty("block.on.buffer.full", "true");
				
		this.publishTopic = publishTopic;
		
		
	}

	@Override
	public void message(CharSequence topic, PayloadReader payload) {
		
		KafkaProducer producer=null;
		try {
			producer = new KafkaProducer(properties);
			
			String originalTopic = payload.readUTF();
			byte[] buffer = new byte[payload.available()];
			payload.read(buffer);
			
			producer.send(new ProducerRecord(publishTopic, buffer));
		
	   } finally {
		    if (null!=producer) {
		    	producer.close();
		    }
	   }
		
	}

}
