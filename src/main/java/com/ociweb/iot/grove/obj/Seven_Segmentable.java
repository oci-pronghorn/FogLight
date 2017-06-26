package com.ociweb.iot.grove.obj;

import com.ociweb.iot.maker.FogCommandChannel;

public interface Seven_Segmentable {
	
	
	
	/**
	 * print a digit (0 through 9) at position (0 through subclass.seven_segmentable_locs - 1).
	 * @param digit
	 * @param position
	 */
	public void printDigitAt(int digit, int position);
	

	/**
	 * switch on or off the colon on the next digit print
	 * @param on
	 */
	public void switchColon(boolean on);
	
	/**
	 * each digit is composed of 7 segment. The numbering system: the topmost segment
	 * is segment 1. We number the 6 outer segments in an ascending clockwise fashion. The segment
	 * in the middle is hence segment 7. We use the last 7 bits of the byte and ignore the
	 * first bit. The least significant bit is segment7. 0b0111_1111 maps to an "8" shape.
	 * 0b0111_1101 maps to a "6" shape.
	 * 
	 * @param b is the byte containing the bitmap
	 * @param position is which digit to map to. Since there are four positions on the board,
	 * valid inputs range from 0 through subclass.seven_segmentable_locs - 1
	 */
	public void drawDigitalBitmapAt(byte b, int position);
	
	public void clearDisplay();
	
	public void setCommandChannel(FogCommandChannel c);
}
