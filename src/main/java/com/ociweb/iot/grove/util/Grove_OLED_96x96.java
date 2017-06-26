package com.ociweb.iot.grove.util;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;

public class Grove_OLED_96x96 implements IODevice {
	
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
		return 0;
	}

}
