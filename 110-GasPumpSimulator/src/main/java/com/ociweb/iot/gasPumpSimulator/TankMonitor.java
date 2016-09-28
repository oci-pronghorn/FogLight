package com.ociweb.iot.gasPumpSimulator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadWriter;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.util.Appendables;

public class TankMonitor implements AnalogListener{

	
	private final CommandChannel commandChannel;
    private final String topic;
    private final int fullTank;
    private final Logger logger = LoggerFactory.getLogger(TankMonitor.class);
	
	public TankMonitor(DeviceRuntime runtime, String topic, int fullTank) {
	
		this.commandChannel = runtime.newCommandChannel();
		this.topic = topic;
		this.fullTank = fullTank;
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
				
		if (value>fullTank) {
			logger.warn("check equipment, tank is deeper than expected");			
		} else {
			int remainingDepth = fullTank-value;
						
	        PayloadWriter payload = commandChannel.openTopic(topic);
			payload.writeLong(time); //local time, may be off, do check the os
			payload.writeInt(remainingDepth);
			payload.publish();
			
			boolean debug = false;
			if (debug) {
				viewOnDevice(remainingDepth);
			}
			
		}
		
	}

	private void viewOnDevice(int remainingDepth) {
		StringBuilder builder = new StringBuilder();
		try {
			Appendables.appendFixedDecimalDigits(builder, remainingDepth, 100);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		builder.append("cm \n");
		builder.append("depth");
		
		//if you would like to show on console
		System.out.println(builder);
		
		//if you would like to show on LCD (if its attached to i2c port)
		//Grove_LCD_RGB.commandForColor(commandChannel, 200, 200, 180);
		//Grove_LCD_RGB.commandForText(commandChannel, builder);
	}

}
