package com.ociweb.iot.valveManifold;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

import static com.ociweb.iot.valveManifold.schema.ValveSchema.*;


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
			long timeStamp = Pipe.takeLong(input);

			switch(msgIdx) {
				case MSG_VALVESERIALNUMBER_311:
				case MSG_LIFECYCLECOUNT_312:
				case MSG_SUPPLYPRESSURE_313:
				case MSG_DURATIONOFLAST1_4SIGNAL_314:
				case MSG_DURATIONOFLAST1_2SIGNAL_315:
				case MSG_EQUALIZATIONAVERAGEPRESSURE_316:
				case MSG_EQUALIZATIONPRESSURERATE_317:
				case MSG_RESIDUALOFDYNAMICANALYSIS_318:
				case MSG_VALVEFAULT_340:
				case MSG_LEAKFAULT_360:
				case MSG_DATAFAULT_362:
				case MSG_PRESSUREPOINT_319: {
					int value = Pipe.takeInt(input);
					data = new byte[12]; //must be new
					write64(data, 0, timeStamp);
					write32(data, 8, value);
					break;
				}
				case MSG_PRESSUREFAULT_350:
				case MSG_PARTNUMBER_330: {
					int meta = Pipe.takeRingByteMetaData(input);
					int len = Pipe.takeRingByteLen(input);
					int pos = Pipe.bytePosition(meta, input, len);
					data = new byte[len + 8]; //must be new
					write64(data, 0, timeStamp);
					Pipe.copyBytesFromToRing(Pipe.blob(input), pos, Pipe.blobMask(input),
							data, 8, Integer.MAX_VALUE, len);
					break;
				}
				default:
					//throw new UnsupportedOperationException("unexpected msg size "+size);
			}
						
			mqttTopic.setLength(0);
			
			mqttTopic.append(root).append('/').append(clientId);
			Appendables.appendValue(mqttTopic, "/",stationId,"/");			
			mqttTopic.append(dataTopic);
						
			//System.err.println(mqttTopic);
			
			if (message(mqttTopic,data)) {
				mqttTopic.setLength(0);
				data = null;
			}
			
			Pipe.confirmLowLevelRead(input, size);
			Pipe.releaseReadLock(input);

		}
	}


	private static int write64(byte[] buf, int pos, long v) {
		buf[pos++] = (byte)(v >>> 64);
		buf[pos++] = (byte)(v >>> 56);
		buf[pos++] = (byte)(v >>> 48);
		buf[pos++] = (byte)(v >>> 32);
		buf[pos++] = (byte)(v >>> 24);
		buf[pos++] = (byte)(v >>> 16);
		buf[pos++] = (byte)(v >>> 8);
		buf[pos++] = (byte) v;
		return pos;
	}
	
    private static int write32(byte[] buf, int pos, int v) {
        buf[pos++] = (byte)(v >>> 24);
        buf[pos++] = (byte)(v >>> 16);
        buf[pos++] = (byte)(v >>> 8);
        buf[pos++] = (byte) v;
        return pos;
    }    
    
	int errorCount = 0;
	
	public boolean message(StringBuilder topic, byte[] data) {

	    try {
		    	if (null==client) {
		    		client = new MqttClient(serverURI, clientId, new MemoryPersistence());
		    		client.connect(connOptions);
		    		client.setTimeToWait(-1);
		    	}

		        MqttMessage message = new MqttMessage();
		        	
		        message.setPayload(data);
		        message.setRetained(retained);
		        message.setQos(QOS);
	
		
		        nextMessageTime = System.currentTimeMillis()+TIME_LIMIT;
		        
		        client.publish(topic.toString(), message);
		        errorCount=0;

		        logger.info("publish MQTT QOS: {} topic: {}",QOS, topic);
		        return true;
	      } catch (MqttException e) {
	    	
	    	if (e.getMessage().contains("is not connected")) {
	    		client = null;
	    		return false;
	    	}
	    	  
	    	try {
				client.disconnect();
			} catch (MqttException e1) {
				//ignore
			}
	    	  client = null;
	    	  if (++errorCount>10) {
	    		  logger.warn("Unable to send payload, is the MQTT broaker {} up and running?",serverURI,e);
	    	  }
	    	  return false;
	      }
	}


	
}
