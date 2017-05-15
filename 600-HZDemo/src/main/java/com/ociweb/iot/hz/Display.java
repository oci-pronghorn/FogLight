package com.ociweb.iot.hz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.impl.PayloadReader;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;

public class Display implements PubSubListener{

	private final CommandChannel commandChannel;
	private String text="";//prevent null
	private final static Logger logger = LoggerFactory.getLogger(Display.class);
	
	public Display(DeviceRuntime runtime) {
		commandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
	}

	@Override
	public boolean message(CharSequence topic, PayloadReader payload) {
		
		
		String newText = payload.readUTF();
		
		logger.info("display request to show text {}",newText);
		
		if (!text.equals(newText)) {
			
			Grove_LCD_RGB.commandForColor(commandChannel, 255, 255, 200);
			if (Grove_LCD_RGB.commandForText(commandChannel, newText)) {
				text = newText;
			}
			
		}
		
		return true;
		
	}

}
