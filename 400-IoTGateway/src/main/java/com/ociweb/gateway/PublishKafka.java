package com.ociweb.gateway;

import java.util.Date;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.pronghorn.util.Appendables;

public class PublishKafka implements PubSubListener {

	private static final Logger logger = LoggerFactory.getLogger(PublishKafka.class);
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
				
		String sensorTopic = payload.readUTF();

		CharSequence[] topicNibbles = Appendables.split(sensorTopic, '/');
		
		//[0] will be the root
		//[1] will be the name of the station
		//[2] will be the type of message either pump or tank
		
		StringBuilder builder = new StringBuilder();
		
		if ("total".equals(topicNibbles[2])) { //purchase total
			long time = payload.readLong();
			String fuelName = payload.readUTF();
			int priceInCents = payload.readInt();
			int centiUnits = payload.readInt(); 
			
			builder.append(new Date(time)+" "+fuelName+" price "+priceInCents+" units "+centiUnits);
			
		}
		
		if ("tank".equals(topicNibbles[2])) {
			long time = payload.readLong();
			int volumCM2 = payload.readInt();
			String fuelName = payload.readUTF();
			
			builder.append(new Date(time)+" volume "+volumCM2+" "+fuelName);
			
		}
		
		
		System.out.println("got message "+sensorTopic+"  payload  "+builder.toString());
		

		KafkaProducer producer=null;
		try {
			producer = new KafkaProducer(properties);
			producer.send(new ProducerRecord(sensorTopic, builder.toString()));
		} catch (Throwable e) {
			//logger.warn("unable to send to kafka.",e);
	    } finally {
		    if (null!=producer) {
		    	producer.close();
		    }
	    }
		
	}

}
