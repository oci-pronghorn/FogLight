package com.ociweb.iot.hz;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.StartupListener;

public class PhysicalWatcher implements AnalogListener, DigitalListener, StartupListener  {

	private long id;
	private int lastDistance;
	private int recordedDistance;
	
    private HazelcastInstance hazelcastInstance;
	 
	
	public PhysicalWatcher(DeviceRuntime runtime) {
	
	}

	@Override
	public void startup() {

		Config config = new Config();
		
		hazelcastInstance = Hazelcast.newHazelcastInstance(config );		

		IdGenerator idGen = hazelcastInstance.getIdGenerator("watchKeys");
		
		id = idGen.newId();
				
	}	
	
	
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
				
		if (value != recordedDistance) {
			
			IMap<Long, Integer> map = hazelcastInstance.getMap("watchMap");
			
			int delta = value-recordedDistance;
			
			map.put(id, delta);
			
		}
				
		lastDistance = value;
				
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
				
		if (1==value) {
			
			recordedDistance = lastDistance;
			
		}
				
	}




}
