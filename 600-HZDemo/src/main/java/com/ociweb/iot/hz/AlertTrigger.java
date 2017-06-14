package com.ociweb.iot.hz;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class AlertTrigger implements TimeListener, StartupListener{

	private HazelcastInstance hazelcastInstance;
	private final FogCommandChannel commandChannel;
	private final Port port;
	private final String displayTopic;
	private final static Logger logger = LoggerFactory.getLogger(AlertTrigger.class);
	//private boolean lastAlert = false;
	
	public AlertTrigger(FogRuntime runtime, Port alertPort, String displayTopic) {
	
		this.commandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		this.port = alertPort;
		this.displayTopic = displayTopic;
		
	}
	
	@Override
	public void startup() {

		Config config = new Config();		
		hazelcastInstance = Hazelcast.newHazelcastInstance(config );		
		logger.info("finished hz startup");
		
	}
	
	@Override
	public void timeEvent(long time, int iteration) {
		
		IMap<Long, Integer> map = hazelcastInstance.getMap("watchMap");
		
		//StringBuilder b = new StringBuilder();
		boolean alert = true;
		for(Map.Entry<Long, Integer> entry: map.entrySet()) {			
			alert = alert & (Math.abs(entry.getValue())>1); //if every value is not zero then we alert.
			logger.info("id {} value {} ", entry.getKey(), entry.getValue());			
		}
				
		if (alert) { 
			commandChannel.setValueAndBlock(port, 1, 200);
			while (!commandChannel.setValue(port, 0)) {
				Thread.yield();
			};
		}
		
	}

	
	
}
