package com.ociweb.iot.grove.display;

public interface Digitalable {	
	public void printDigitAt(int digit, int position);
	
	
	public void printCharAt(char c, int position);
	
	
	
	public void switchTopDot(boolean on);
	
	/**
	 * used to switch on or off the bottom dot
	 * @param on
	 */
	public void switchBototmDot(boolean on);
	
	/**
	 * each digit is composed of 7 lines. The numbering system: the topmost line
	 * is line 1. We number the 6 outer lines in an ascending clockwise fashion. The line
	 * in the middle is hence line 7. We use the last 7 bits of the byte and ignore the
	 * first bit. The least significant bit is line7. 0b0111_1111 maps to an "8" shape.
	 * 0b0101_1111 maps to a "6" shape.
	 * 
	 * @param b is the byte containing the bitmap
	 * @param position is which digit to map to. Since there are four positions on the board,
	 * valid inputs range from 0 to 3.
	 */
	public void drawDigitalBitmapAt(byte b, int position);
	
}
