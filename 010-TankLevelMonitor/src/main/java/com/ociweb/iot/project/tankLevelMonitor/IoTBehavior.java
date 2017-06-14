package com.ociweb.iot.project.tankLevelMonitor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.util.Appendables;

public class IoTBehavior implements DigitalListener, AnalogListener{

	private Logger logger = LoggerFactory.getLogger(IoTBehavior.class);
	
	private final FogCommandChannel channel;
	
	private final int fullTank = 13;
	
	public IoTBehavior(FogRuntime runtime) {

   	  channel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
   	 
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		
		System.out.println("button "+value);
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		
		if (value>fullTank) {
			logger.warn("check equipment, tank is deeper than expected");			
		} else {
			int remainingDepth = fullTank-value;
			
			StringBuilder builder = new StringBuilder();
			Appendables.appendFixedDecimalDigits(builder, remainingDepth, 100);

			builder.append("cm \n");
			builder.append("depth");
			
			Grove_LCD_RGB.commandForColor(channel, 200, 200, 180);
			Grove_LCD_RGB.commandForText(channel, builder);
			
			
		}
		
		
		
	}

}
