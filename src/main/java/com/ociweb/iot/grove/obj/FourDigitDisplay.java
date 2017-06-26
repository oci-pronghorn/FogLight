package com.ociweb.iot.grove.obj;


import com.ociweb.iot.grove.util.Grove_FourDigitDisplay;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.Port;



/**
 * Object class holding onto the FogCommandChannel and Port used by the Grove_FourDigitDisplay class
 * @author Ray Lo, Nathan Tippy
 *
 */
public class FourDigitDisplay {
	
	private final FogCommandChannel ch;
	private final Port p;
	
	public FourDigitDisplay(FogCommandChannel ch, Port p){
		this.ch = ch;
		this.p = p;
	}
	
	public boolean init(){
		return Grove_FourDigitDisplay.init(ch,p);
	}

	public boolean printDigitAt(int index, int digit) {
		return Grove_FourDigitDisplay.printDigitAt(this.ch, this.p,index, digit);
	}

	public boolean printFourDigitsWithColon(int leftPair, int rightPair){
		return Grove_FourDigitDisplay.printFourDigitsWithColon(this.ch, this.p, leftPair, rightPair);
	}
	
	public boolean setBrightness(int brightness){
		return Grove_FourDigitDisplay.setBrightness(ch, p, brightness);
	}
	
	public boolean displayOn(){
		return Grove_FourDigitDisplay.displayOn(this.ch, this.p);
	}
	
	public boolean displayOff(){
		return Grove_FourDigitDisplay.displayOff(this.ch, this.p);
	}

//	/**
//	 * Calls {@link com.ociweb.iot.grove.Grove_FourDigitDisplay#drawBitmapAt(FogCommandChannel, byte, int, boolean)}
//	 * @param b
//	 * @param position
//	 */
//	@Override
//	public void drawDigitalBitmapAt(byte b, int position) {
//		Grove_FourDigitDisplay.drawBitmapAt(this.target, b, position, colon_on);
//
//	}
//
//	/**
//	 * Switch colon_on field on or off so that the next digit print or bitmap draw will
//	 * turn on or off the colon.
//	 * @param on true switches the colon on, false switches the colon off, on the next print event.
//	 */
//	@Override
//	public void switchColon(boolean on) {
//		colon_on = true;
//	}
//
//	/**
//	 * Calls {@link com.ociweb.iot.grove.Grove_FourDigitDisplay#clearDisplay(FogCommandChannel)}
//	 */
//	@Override
//	public void clearDisplay() {
//		
//		Grove_FourDigitDisplay.clearDisplay(this.target);
//		
//	}

}
