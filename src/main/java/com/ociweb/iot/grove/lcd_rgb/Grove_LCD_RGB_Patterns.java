package com.ociweb.iot.grove.lcd_rgb;

/**
 * Various static byte arrays for common shapes that can be displayed
 * on a Grove LCD RGB display
 * 
 * @author Nathan Tippy
 */
public class Grove_LCD_RGB_Patterns {

	// Suits //////////////////////////////////////////////////////////////////
	public static final byte[] spade = {
			0b00100,
			0b01110,
			0b11111,
			0b11111,
			0b11111,
			0b10101,
			0b00100,
			0b01110,	
	};
	
	public static final byte[] heart = {
			0b00000,
			0b01010,
			0b11111,
			0b11111,
			0b11111,
			0b01110,
			0b00100,
			0b00000,	
	};
	
	public static final byte[] club = {
			0b00000,
			0b01110,
			0b01110,
			0b11111,
			0b11111,
			0b11111,
			0b00100,
			0b01110,	
	};
	
	public static final byte[] diamond = {
			0b00000,
			0b00100,
			0b01110,
			0b11111,
			0b11111,
			0b11111,
			0b01110,
			0b00100,	
	};
	
	// Emojis /////////////////////////////////////////////////////////////////
	public static final byte[] smiley = {
			0b00000,
			0b01010,
			0b01010,
			0b01010,
			0b00000,
			0b10001,
			0b10001,
			0b01110,	
	};
	
	public static final byte[] sad = {
			0b00000,
			0b01010,
			0b01010,
			0b01010,
			0b00000,
			0b01110,
			0b10001,
			0b10001,	
	};
	
	public static final byte[] surprised = {
			0b00000,
			0b01010,
			0b01010,
			0b00000,
			0b01110,
			0b10001,
			0b10001,
			0b01110,	
	};
	
	public static final byte[] happy = {
			0b00000,
			0b01010,
			0b01010,
			0b00000,
			0b11111,
			0b10001,
			0b10001,
			0b01110,	
	};
	
	public static final byte[] angry = {
			0b10001,
			0b01010,
			0b00000,
			0b01010,
			0b00000,
			0b01110,
			0b10001,
			0b10001,	
	};
	
	public static final byte[] sunglasses = {
			0b00000,
			0b00000,
			0b11111,
			0b11011,
			0b00000,
			0b10001,
			0b01110,
			0b00000,
			0b00000,	
	};
	
	public static final byte[] expressionless = {
			0b00000,
			0b00000,
			0b11011,
			0b00000,
			0b11111,
			0b00000,
			0b00000,
			0b00000,	
	};

	// Arrows /////////////////////////////////////////////////////////////////
	public static final byte[] upArrow = {
			0b00000,
			0b00100,
			0b01110,
			0b10101,
			0b00100,
			0b00100,
			0b00000,
			0b00000,	
	};
	
	public static final byte[] downArrow = {
			0b00000,
			0b00100,
			0b00100,
			0b10101,
			0b01110,
			0b00100,
			0b00000,
			0b00000,	
	};
	
	public static final byte[] leftArrow = {
			0b00000,
			0b00100,
			0b01000,
			0b11111,
			0b01000,
			0b00100,
			0b00000,
			0b00000,	
	};
	
	public static final byte[] rightArrow = {
			0b00000,
			0b00100,
			0b00010,
			0b11111,
			0b00010,
			0b00100,
			0b00000,
			0b00000,	
	};
	
	
	// Patterns ///////////////////////////////////////////////////////////////
	public static final byte[] dithered = {
			0b10101,
			0b01010,
			0b10101,
			0b01010,
			0b10101,
			0b01010,
			0b10101,
			0b01010
	};
	
	public static final byte[] blocked = {
			0b11111,
			0b11111,
			0b11111,
			0b11111,
			0b11111,
			0b11111,
			0b11111,
			0b11111
	};
}
