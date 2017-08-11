package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

import static com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Twig.*;

public class LCDRGB_Behavior implements Behavior, StartupListener {

	private LCD_RGB_Transducer lcd;

	
	public LCDRGB_Behavior(FogRuntime rt){
		lcd = LCD_RGB.newTransducer(rt.newCommandChannel());	
	}


	@Override
	public void startup() {
		lcd.setCursor(0, 0);
		lcd.commandForTextAndColor("Hello Wall!", 63, 63, 63);
	}
	

	
		
	

}
