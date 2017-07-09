package com.coiweb.oe.foglight.api;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.iot.maker.SerialReader;

public class SerialListenerBehavior implements SerialListener {

	private final FogRuntime runtime;
	private final static Logger logger = LoggerFactory.getLogger(SerialListenerBehavior.class);
	
	private byte[] myBuffer = new byte[10];
	private int timeToLive = 10;
	
	public SerialListenerBehavior(FogRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public int message(SerialReader reader) {
		
		if (reader.available()<10) {
			return 0; //consumed nothing
		} else {
						
			int consumed = reader.read(myBuffer);
		
			logger.info("consumed data {} ", Arrays.toString(myBuffer));
			
			if (--timeToLive <= 0) {
				runtime.shutdownRuntime();
			}
			
			return consumed;			
		}
		
	}

}
