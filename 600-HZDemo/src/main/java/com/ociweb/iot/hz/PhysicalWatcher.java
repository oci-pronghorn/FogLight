package com.ociweb.iot.hz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Port;

public class PhysicalWatcher implements AnalogListener, DigitalListener, StartupListener  {

	private long id;
	private int lastDistance;
	private int recordedDistance;
	private final String displayTopic;
	
    private HazelcastInstance hazelcastInstance;
	private FogCommandChannel commandChannel;
	 
	private final static Logger logger = LoggerFactory.getLogger(PhysicalWatcher.class);
	
	public PhysicalWatcher(FogRuntime runtime, String displayTopic) {
		this.displayTopic = displayTopic;
		this.commandChannel = runtime.newCommandChannel(MsgCommandChannel.DYNAMIC_MESSAGING);
	}

	@Override
	public void startup() {

		Config config = new Config();
		
		hazelcastInstance = Hazelcast.newHazelcastInstance(config );		

		logger.info("finished hz startup");
		IdGenerator idGen = hazelcastInstance.getIdGenerator("watchKeys");
		
		id = idGen.newId();
		logger.info("our id is {}",id);
		
				
	}	
	
	
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
				
		if (value != recordedDistance) {
			
			IMap<Long, Integer> map = hazelcastInstance.getMap("watchMap");
			
			int delta = value-recordedDistance;
			
			map.put(id, delta);
			
		}
		
		String text = "local: "+recordedDistance+"   "+value;
		
		commandChannel.publishTopic(displayTopic, w-> {
			w.writeUTF(text);			
		});
						
		lastDistance = value;
				
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
				
		if (1==value) {
			recordedDistance = lastDistance;
			logger.info("record new distance for {} of {}",id,recordedDistance);
		}
				
	}




}
