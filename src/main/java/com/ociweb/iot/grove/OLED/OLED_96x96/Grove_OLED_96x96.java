package com.ociweb.iot.grove.OLED.OLED_96x96;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;


public class Grove_OLED_96x96 implements IODevice {
	
	public static final Grove_OLED_96x96 instance = new Grove_OLED_96x96();
	
	private Grove_OLED_96x96(){
	}
	
	public OLED_96x96 newObj(FogCommandChannel ch){
		return new OLED_96x96(ch);
	}

	@Override
	public int response() {		
		return 0;
	}

	@Override
	public int scanDelay() {
		return 0;
	}

	@Override
	public boolean isInput() {
		return false;
	}

	@Override
	public boolean isOutput() {
		return true;
	}

	@Override
	public boolean isPWM() {
		return false;
	}

	@Override
	public int range() {
		return 0;
	}

	@Override
	public I2CConnection getI2CConnection() {
		return null;
	}

	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return false;
	}

	@Override
	public int pinsUsed() {
		return 1;
	}

}
