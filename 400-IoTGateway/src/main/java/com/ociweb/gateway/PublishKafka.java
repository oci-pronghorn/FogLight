package com.ociweb.gateway;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.pronghorn.util.Appendables;

public class PublishKafka implements PubSubListener {

	private static final Logger logger = LoggerFactory.getLogger(PublishKafka.class);
	private final Properties properties=new Properties();
	private final String kafkaTopic = new String("open24");
	private final long MILLISECONDS_PER_SECOND = 1000;
	private final long EXPANDED_TIME_SLOPE = 60;
	private final long EXPANDED_TIME_OFFSET = 10;
	private long lastEpochSeconds = 0;
	private long expandedEpochSeconds = 0;
	private String stationId = new String("unknownStation");

	public PublishKafka(DeviceRuntime runtime, String kafkaURI, String clientId) {
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaURI);
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, "true");
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
		stationId = clientId;
	}

	private long elongateTime(long epochSeconds) {

		if(lastEpochSeconds == 0) {
			lastEpochSeconds = epochSeconds;
			expandedEpochSeconds = epochSeconds;
		}

		long deltaSeconds = epochSeconds - lastEpochSeconds;
		lastEpochSeconds = epochSeconds;

		expandedEpochSeconds += ((deltaSeconds * EXPANDED_TIME_SLOPE) + EXPANDED_TIME_OFFSET);

		return expandedEpochSeconds;

	}

	private String createPriceMessage(long epochSeconds, String fuelName, int priceInCents){
		// price.{{ station_no }} {{ timestamp }} {{ value }} station={{ station_no } type={{ regular | premium | diesel}}
		StringBuilder builder = new StringBuilder();
		builder.append("price.").append(stationId).append(' ');
		builder.append(Long.toString(elongateTime(epochSeconds))).append(' ');
		builder.append(priceInCents).append(' ');
		builder.append("station=").append(stationId).append(' ');
		builder.append("type=").append(fuelName);

		return builder.toString();
	}

	private String createVolumeMessage(String pumpId, long epochSeconds, String fuelName, int totalUnits){
		// volume.{{ station_no }} {{ timestamp }} {{ value }} pump={{ pump_no }} type={{ regular | premium | diesel }}
		StringBuilder builder = new StringBuilder();
		builder.append("volume.").append(stationId).append(' ');
		builder.append(Long.toString(elongateTime(epochSeconds))).append(' ');
		builder.append(totalUnits).append(' ');
		builder.append("pump=").append(pumpId).append(' ');
		builder.append("type=").append(fuelName);
		return builder.toString();
	}

	private String createLevelMessage(long epochSeconds, int volumCM2, String fuelName){

		// level.{{ station_no }} {{ timestamp }} {{ value }} station={{ station_no }} type={{ regular | premium | diesel }}
		StringBuilder builder = new StringBuilder();
		builder.append("level.").append(stationId).append(' ');
		builder.append(Long.toString(elongateTime(epochSeconds))).append(' ');
		builder.append(volumCM2).append(' ');
		builder.append("station=").append(stationId).append(' ');;
		builder.append("type=").append(fuelName);
		return builder.toString();
	}

	@Override
	public void message(CharSequence topic, PayloadReader payload) {

		String sensorTopic = payload.readUTF();

		CharSequence[] topicSubArray = Appendables.split(sensorTopic, '/');
		//[0] will be the root
		//[1] will be the pumpId
		//[2] will be the type of message either pump or tank

		List<String> values = new ArrayList<>();

		if ("total".equals(topicSubArray[2])) { //purchase total

			long epochSeconds = payload.readLong()/MILLISECONDS_PER_SECOND;
			String fuelName   = payload.readUTF();
			int priceInCents  = payload.readInt(); // dollers * 100 or pennies
			int totalUnits    = payload.readInt(); // units * 100
			String pumpId = topicSubArray[1].toString();

			values.add(createPriceMessage(epochSeconds, fuelName, priceInCents));
			values.add(createVolumeMessage(pumpId, epochSeconds, fuelName, totalUnits));
		}

		if ("tank".equals(topicSubArray[2])) {

			long epochSeconds = payload.readLong()/MILLISECONDS_PER_SECOND;
			int volumCM2 = payload.readInt();
			String fuelName = payload.readUTF();

			values.add(createLevelMessage(epochSeconds, volumCM2, fuelName));
		}

		KafkaProducer<String,String> producer = null;
		try {
			producer = new KafkaProducer<String,String>(properties);
			for(String value:values){
				System.out.println("got MQTT topic:" + sensorTopic + " kafkaTopic:" + kafkaTopic + " payload:" + value);
				producer.send(new ProducerRecord<String,String>(kafkaTopic, kafkaTopic, value));
			}
		} catch (Throwable e) {
			//logger.warn("unable to send to kafka.",e);
	    } finally {
		    if (null!=producer) {
		    	producer.close();
		    }
	    }
	}

}
