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

public class TankMonitor implements AnalogListener {
	
	private final CommandChannel commandChannel;
    private final String topic;
    private final int fullTank;
    private final int radiusMM = 42;
    private final int radiusSquared = radiusMM*radiusMM;;
    private final double radiusSquaredPi = Math.PI*radiusSquared;
    
    
    
    private final String fuelName;
    private final Logger logger = LoggerFactory.getLogger(TankMonitor.class);
	
	public TankMonitor(DeviceRuntime runtime, String topic, int fullTank, String fuelName) {
	
		this.commandChannel = runtime.newCommandChannel();
		this.topic = topic;
		this.fuelName = fuelName;
		this.fullTank = fullTank;
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
			
		if (value>fullTank) {
			logger.warn("check equipment, tank {} is deeper than expected {} ",value,fullTank);			
		} else {
						
			int volumeCM = computeVolumeCM2(value);

	        PayloadWriter payload = commandChannel.openTopic(topic);
	        	        
			payload.writeLong(time); //local time, may be off, do check the os
			payload.writeInt(volumeCM);
			payload.writeUTF(fuelName);
			
			payload.publish();
			
			boolean debug = false;
			if (debug) {
				viewOnDevice(volumeCM);
			}
			
		}
		
	}

	private int computeVolumeCM2(int value) {
		int remainingDepthMM =  10*(fullTank-value);		
		double volumeMM2 = radiusSquaredPi * remainingDepthMM;
		int volumeCM = (int) Math.rint(volumeMM2/100d);
		return volumeCM;
	}

	private void viewOnDevice(int remainingDepth) {
		StringBuilder builder = new StringBuilder();
		Appendables.appendFixedDecimalDigits(builder, remainingDepth, 100);

		builder.append(" mm^2 volume\n");
		builder.append(fuelName);
		
		//if you would like to show on console
		System.out.println(builder);
		
		//if you would like to show on LCD (if its attached to i2c port)
		Grove_LCD_RGB.commandForColor(commandChannel, 200, 200, 180);
		Grove_LCD_RGB.commandForText(commandChannel, builder);
	}


}
