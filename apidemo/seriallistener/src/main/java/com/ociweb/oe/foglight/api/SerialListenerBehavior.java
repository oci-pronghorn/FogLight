package com.ociweb.oe.foglight.api;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.util.Appendables;

public class SerialListenerBehavior implements SerialListener {

	private final FogRuntime runtime;
	private final static Logger logger = LoggerFactory.getLogger(SerialListenerBehavior.class);
	
	private byte[] myBuffer = new byte[10];
	private int timeToLive = 2;
	private final Appendable builder;
	
	SerialListenerBehavior(Appendable builder, FogRuntime runtime) {
		this.runtime = runtime;
		this.builder = builder;
	}

	@Override
	public int message(ChannelReader reader) {
		
		if (reader.available()<10) {
			return 0; //consumed nothing
		} else {
						
			int consumed = reader.read(myBuffer);
		
			Appendables.appendArray(builder, '[', myBuffer, ']');
	
			if (--timeToLive <= 0) {
				runtime.shutdownRuntime();
			}
			
			return consumed;			
		}
		
	}

}
