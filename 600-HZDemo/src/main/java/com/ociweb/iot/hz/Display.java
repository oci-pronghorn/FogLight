package com.ociweb.iot.hz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;

public class Display implements PubSubListener{

	private final CommandChannel commandChannel;
	private String text="";//prevent null
	private final static Logger logger = LoggerFactory.getLogger(Display.class);
	
	public Display(DeviceRuntime runtime) {
		commandChannel = runtime.newCommandChannel();
	}

	@Override
	public void message(CharSequence topic, PayloadReader payload) {
		
		
		String newText = payload.readUTF();
		
		logger.info("display request to show text {}",newText);
		
		if (!text.equals(newText)) {
			
			Grove_LCD_RGB.commandForColor(commandChannel, 255, 255, 200);
			if (Grove_LCD_RGB.commandForText(commandChannel, newText)) {
				text = newText;
			}
			
		}
		
		
		
	}

}
