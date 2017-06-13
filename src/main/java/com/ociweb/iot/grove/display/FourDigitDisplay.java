package com.ociweb.iot.grove.display;


import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.grove.Grove_FourDigitDisplay;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;


/**
 * OOP Wrapper class around the Grove_FourDigitDisplay class
 * @author Ray Lo
 *
 */
public class FourDigitDisplay implements Seven_Segmentable, IODevice  {
	
	private CommandChannel target;
	private boolean colon_on = false;
	
	public FourDigitDisplay(CommandChannel c){
		this.target = c;
	}
	
	/**
	 * Calls {@link com.ociweb.iot.grove.Grove_FourDigitDisplay#printDigitAt(int, int, boolean)}
	 * @param digit
	 * @param position 
	 */
	@Override
	public void printDigitAt(int digit, int position) {
		Grove_FourDigitDisplay.printDigitAt(this.target, digit, position, colon_on);
	}

	/**
	 * Calls {@link com.ociweb.iot.grove.Grove_FourDigitDisplay#drawBitmapAt(CommandChannel, byte, int, boolean)}
	 * @param b
	 * @param position
	 */
	@Override
	public void drawDigitalBitmapAt(byte b, int position) {
		Grove_FourDigitDisplay.drawBitmapAt(this.target, b, position, colon_on);

	}

	/**
	 * Switch colon_on field on or off so that the next digit print or bitmap draw will
	 * turn on or off the colon.
	 * @param on true switches the colon on, false switches the colon off, on the next print event.
	 */
	@Override
	public void switchColon(boolean on) {
		colon_on = true;
	}

	/**
	 * Calls {@link com.ociweb.iot.grove.Grove_FourDigitDisplay#clearDisplay(CommandChannel)}
	 */
	@Override
	public void clearDisplay() {
		
		Grove_FourDigitDisplay.clearDisplay(this.target);
		
	}

	/**
	 * Setter for the single CommandChannel associated with the four-digit display
	 * @param c
	 */
	@Override
	public void setCommandChannel(CommandChannel c) {
		this.target = c;
		
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
		return true;
	}
	@Override
	public int pinsUsed() {
		return 2;
	}

}
