package com.ociweb.iot.grove.OLED.OLED_128x64;

import com.ociweb.iot.hardware.I2CConnection;

import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;


/**
 * Singleton class that defines basic properties of the display. Main purpose is to inialize and return a OLED_128x64
 * object for the user.
 * @author Ray Lo, Nathan Tippy
 *
 */
public class Grove_OLED_128x64 implements IODevice{
	
	
	public static final Grove_OLED_128x64 instance = new Grove_OLED_128x64();
	
	/**
	 * Private constructor for singleton design pattern.
	 */
	private Grove_OLED_128x64(){
	}


	/**
	 * Dynamically allocates an instance of {@link OLED_128x64}
	 * @param ch {@link FogCommandChannel} reference to be held onto by the new {@link OLED_128x64}
	 * @return the new instance of {@link OLED_128x64} created.
	 */
	public static OLED_128x64 newObj(FogCommandChannel ch){
		return new OLED_128x64(ch);
	}
	
	@Override
	public int response() {
		return 20;
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
		return true;
	}

	@Override
	public int pinsUsed() {
		return 1;
	}

}
