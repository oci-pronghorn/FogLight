package com.ociweb.iot.hz;

import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;

public class AlertTrigger implements TimeListener, StartupListener{

	private HazelcastInstance hazelcastInstance;
	private final CommandChannel commandChannel;
	private final Port port;
	
	public AlertTrigger(DeviceRuntime runtime, Port alertPort) {
	
		this.commandChannel = runtime.newCommandChannel();
		this.port = alertPort;
		
	}
	
	@Override
	public void startup() {

		Config config = new Config();
		
		hazelcastInstance = Hazelcast.newHazelcastInstance(config );		

	}
	
	@Override
	public void timeEvent(long time) {
		
		IMap<Long, Integer> map = hazelcastInstance.getMap("watchMap");
		
		boolean alert = true;
		for(Map.Entry<Long, Integer> entry: map.entrySet()) {			
			alert = alert & (entry.getValue()>0);			
		}
		if (alert) {
			
			commandChannel.setValueAndBlock(port, 1, 500);
			commandChannel.setValue(port, 0);
			
		}
		
	}

}
