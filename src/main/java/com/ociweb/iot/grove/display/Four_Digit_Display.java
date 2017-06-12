package com.ociweb.iot.grove.display;

import com.ociweb.iot.hardware.I2CConnection;

import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.grove.Static_Four_Digit_Display;


//for the Four_Digit_Display, let's first try skipping the bottom most Static layer that the
//RGB FOUR_DIG has
public class Four_Digit_Display implements Seven_Segmentable  {
	
	private CommandChannel target;
	private boolean colon_on = false;
	
	@Override
	public void printDigitAt(int digit, int position) {
		Static_Four_Digit_Display.printDigitAt(this.target, digit, position, colon_on);
	}

	@Override
	public void drawDigitalBitmapAt(byte b, int position) {
		Static_Four_Digit_Display.drawBitmapAt(target, b, position, colon_on);

	}

	@Override
	public void switchColon(boolean on) {
		colon_on = true;
	}

	@Override
	public void clearDisplay() {
		Static_Four_Digit_Display.clearDisplay(target);
		
	}


}
