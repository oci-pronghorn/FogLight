package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ociweb.iot.maker.FogRuntime.SERIAL_WRITER;

public class SerialWriterBehavior implements TimeListener {

	private static Logger logger = LoggerFactory.getLogger(SerialWriterBehavior.class);
	private final FogCommandChannel cmd;
	private int value = 0;
	
	public SerialWriterBehavior(FogRuntime runtime) {	
		cmd = runtime.newCommandChannel(SERIAL_WRITER );
	}

	@Override
	public void timeEvent(long time, int iteration) {			
		if (!cmd.publishSerial(writer -> writer.writeByte(value++))) {
			logger.warn("unable to write to serial, the system is too busy");
		}
	}

}
