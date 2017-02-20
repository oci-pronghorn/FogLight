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
	private final boolean retained = true;//gateway must hold last value unil it is replaced.
	private final String serverURI;
	private final String clientId;
	private final String root = "manifold";
	private static final Logger logger = LoggerFactory.getLogger(MQTTPublishPAHOStage.class);

	private static final long TIME_LIMIT = 10_000;// 10 SECONDS
	private long nextMessageTime = System.currentTimeMillis()+TIME_LIMIT;	

	private StringBuilder mqttTopic = new StringBuilder();
	private byte[] data = null;
	
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
		System.exit(-1); //take down the entire app.
	}
	
	@Override
	public void run() {
		
		///////////////////
		//retry old data
		///////////////
		if ((data!=null) && (mqttTopic.length()>0)) {
			//we have old data which was never sent due to network problems, try sending it again.
			if (message(mqttTopic,data)) {
				mqttTopic.setLength(0);
				data = null;
			} else {
				//we had another failure so back-off
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				return;
			}
		}
		//////////////
		//////////////
		
		if (!Pipe.hasContentToRead(input) && System.currentTimeMillis()>nextMessageTime) {
		
			logger.info("no data to publish");			
			nextMessageTime = System.currentTimeMillis()+TIME_LIMIT;

		}
		
		while (Pipe.hasContentToRead(input)) {
					
			
			int msgIdx = Pipe.takeMsgIdx(input);
			
			if (msgIdx<0) {
				Pipe.confirmLowLevelRead(input, Pipe.EOF_SIZE);
				Pipe.releaseReadLock(input);
				requestShutdown();
				return;
			}
			
			int size = Pipe.sizeOf(input, msgIdx);
			
			String dataTopic = Pipe.from(input).fieldNameScript[msgIdx]; //skip over id to the field;
			

			
			int stationId = Pipe.takeInt(input);
			
			switch (size) {
			
				case 3:
					int idx = dataTopic.indexOf('/');
					assert(idx>=0);
					//System.err.println("data to send: "+dataTopic.substring(idx+1));
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
			
			if (message(mqttTopic,data)) {
				mqttTopic.setLength(0);
				data = null;
			}
			
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
    
	
	public boolean message(StringBuilder topic, byte[] data) {

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
		
		        nextMessageTime = System.currentTimeMillis()+TIME_LIMIT;
		        
		        client.publish(topic.toString(), message);
	
		        client.disconnect();
		        logger.info("publish MQTT QOS: {} topic: {}",QOS, topic);
		        return true;
	      } catch (MqttException e) {
	    	  client = null;
	    	  logger.warn("Unable to send payload, is the MQTT broaker {} up and running?",serverURI,e);
	    	  return false;
	      }
	}


	
}
