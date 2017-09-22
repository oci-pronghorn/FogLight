package com.coiweb.oe.foglight.api;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.gl.api.Writable;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.ChannelWriter;

import static com.ociweb.iot.maker.FogRuntime.SERIAL_WRITER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialWriterBehavior implements TimeListener {

	private static Logger logger = LoggerFactory.getLogger(SerialWriterBehavior.class);
	private final FogCommandChannel cmd;
	private int value = 0;

	Writable writable = new Writable() {
		@Override
		public void write(ChannelWriter writer) {
			writer.writeByte(value++);
		}		
	};
	
	public SerialWriterBehavior(FogRuntime runtime) {	
		cmd = runtime.newCommandChannel(SERIAL_WRITER );
	}

	@Override
	public void timeEvent(long time, int iteration) {			
		if (!cmd.publishSerial(writable)) {
			logger.warn("unable to write to serial, the system is too busy");
		}
	}

}
