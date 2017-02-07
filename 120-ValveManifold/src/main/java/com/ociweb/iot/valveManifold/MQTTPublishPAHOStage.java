package com.ociweb.iot.valveManifold;

import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MQTTPublishPAHOStage extends PronghornStage {

	private Pipe input;
	
	private MqttConnectOptions connOptions;
	private MqttClient client;
	private final int QOS = 1;
	private final boolean retained = false;  //TOOD: should this be true??
	private final String serverURI;
	private final String clientId;
	private final String root = "manifold";
	private static final Logger logger = LoggerFactory.getLogger(MQTTPublishPAHOStage.class);
		

	public static void newInstance(GraphManager gm, Pipe<ValveSchema> input, String serverURI, String clientId) {
		new MQTTPublishPAHOStage(gm, input, serverURI, clientId);
	}
	
	protected MQTTPublishPAHOStage(GraphManager graphManager, Pipe input, String serverURI, String clientId) {
		super(graphManager, input, NONE);
		this.input = input;
		this.connOptions = new MqttConnectOptions();
		this.connOptions.setCleanSession(true);
		this.connOptions.setKeepAliveInterval(0);
		this.connOptions.setConnectionTimeout(0);
		
		this.serverURI = serverURI;
		this.clientId = clientId;
	}

	@Override
	public void startup() {
		//logger.info("started up");
	}
	
	@Override
	public void shutdown() {
		//logger.info("shutdown");
	}
	
	@Override
	public void run() {
		FieldReferenceOffsetManager from = Pipe.from(input);
		StringBuilder mqttTopic = new StringBuilder();
		
		
		while (Pipe.hasContentToRead(input)) {
					
			
			int msgIdx = Pipe.takeMsgIdx(input);
			
			if (msgIdx<0) {
				Pipe.confirmLowLevelRead(input, Pipe.EOF_SIZE);
				Pipe.releaseReadLock(input);
				requestShutdown();
				return;
			}
			
			int size = Pipe.sizeOf(input, msgIdx);
			
			String dataTopic = from.fieldNameScript[msgIdx]; //skip over id to the field;
			

			
			int stationId = Pipe.takeInt(input);
			
			byte[] data = null;
			switch (size) {
			
				case 3:
					int idx = dataTopic.indexOf('/');
					assert(idx>=0);
					//the body is in the dataTopic
					data = dataTopic.substring(idx+1).getBytes();
					dataTopic = dataTopic.substring(0, idx);										
					
				break;
				case 4:
					//single int value
					int value = Pipe.takeInt(input);
					data = new byte[4]; //must be new 
					write32(data,0,value);
				break;
				case 5:
					//probably text
					int meta = Pipe.takeRingByteMetaData(input);
					int len = Pipe.takeRingByteLen(input);
					int pos = Pipe.bytePosition(meta, input, len);
					data = new byte[len]; //must be new 
					
					Pipe.copyBytesFromToRing(Pipe.blob(input), pos, Pipe.blobMask(input), 
							                 data, 0, Integer.MAX_VALUE, len);
					
				break;
				default:
					//throw new UnsupportedOperationException("unexpected msg size "+size);
			
			}
						
			mqttTopic.setLength(0);
			
			mqttTopic.append(root).append('/').append(clientId);
			Appendables.appendValue(mqttTopic, "/",stationId,"/");			
			mqttTopic.append(dataTopic);
						
	//		System.err.println(mqttTopic);
			
			message(mqttTopic,data);
			
			Pipe.confirmLowLevelRead(input, size);
			Pipe.releaseReadLock(input);

		}
		
		
	}
	
	
    private static int write32(byte[] buf, int pos, int v) {
        buf[pos++] = (byte)(v >>> 24);
        buf[pos++] = (byte)(v >>> 16);
        buf[pos++] = (byte)(v >>> 8);
        buf[pos++] = (byte) v;
        return pos;
    }
    
    
	
	public void message(StringBuilder topic, byte[] data) {

	    try {
		    	if (null==client) {
		    		client = new MqttClient(serverURI, clientId, new MemoryPersistence());
		    	}

		        MqttMessage message = new MqttMessage();
		        	
		        message.setPayload(data);
		        message.setRetained(retained);
		        message.setQos(QOS);
	
		        client.connect(connOptions);
		        client.setTimeToWait(-1);
	
	
		        logger.info("publish MQTT QOS: {} topic: {}",QOS, topic);
		        
		        client.publish(topic.toString(), message);
	
		        client.disconnect();

	      } catch (MqttException e) {
	    	  client = null;
	    	  logger.warn("Unable to send payload, is the MQTT broaker {} up and running?",serverURI,e);
	      }
	}


	
}
