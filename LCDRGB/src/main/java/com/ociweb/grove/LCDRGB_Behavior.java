package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;

import static com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Twig.*;

public class LCDRGB_Behavior implements Behavior, StartupListener {

	private LCD_RGB_Transducer lcd;

	
	public LCDRGB_Behavior(FogCommandChannel ch){
		lcd = LCD_RGB.newTransducer(ch);
		
	}
	
	@Override
	public void timeEvent(long time, int iteration) {
		
	}

}
