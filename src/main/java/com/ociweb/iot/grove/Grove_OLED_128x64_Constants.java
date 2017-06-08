package com.ociweb.iot.grove;

public class Grove_OLED_128x64_Constants {
	
	/*
	 * NOTE: The bit-mask is there because Java does not have unsigned bytes. 
	 * Hence, it won't consider 0x80 (0d128) for instance, to be a byte as 
	 * it is too large for a signed byte.
	 */
	
	//commands
	
	//these commands set a value for a particular parameter (i.e. brightness) take up
	//a range of values (i.e. 0x00 through 0x0f). The base (lowest valued) command is listed as a consant here.
	
	public static byte LOWER_COL_START_ADDRESS_PAGE_MODE = (byte)(0x00 & 0xFF);//0x00 ~ 0x0f
	public static byte HIGH_COL_START_ADDRESS_PAGE_MODE = (byte)(0x10 & 0xFF); //0x10 ~ 0x1f
	public static byte DISPLAY_START_LINE = (byte)(0x40 & 0xFF); //40~7f
	public static byte PAGE_START_ADDRESS_PAGE_MODE = (byte)(0xB0 & 0xFF);//0xB0 ~ 0x B7
	
	//these commands require further input afterwards
	public static byte SET_MEMORY = (byte)(0x20 & 0xFF);
	public static byte SET_COLUMN_ADDRESS = (byte)(0x21& 0xFF);
	public static byte SET_PAGE_ADDRESS = (byte)(0x22& 0xFF);
	public static byte SET_CONTRAST_CONTROL= (byte)(0x81 & 0xFF);
	public static byte SET_MUX_RATIO = (byte)(0xA8 & 0xFF);
	public static byte SET_EXT_byte_I_REF = (byte)(0xAD & 0xFF);
	public static byte SET_DISPLAY_OFFSET = (byte)(0xD3 & 0xFF);
	public static byte SET_CLOCK_DIV_RATIO = (byte)(0xD5 & 0xFF);
	public static byte SET_PRECHARGE_PERIOD = (byte)(0xD9 & 0xFF);
	public static byte SET_COM_PINS_CONFIG = (byte)(0xDA & 0xFF);
	public static byte SET_VCOM_DESELECT_LEVEL = (byte)(0xDB & 0xFF);
	
	public static byte SET_RIGHT_HOR_SCROLL = (byte)(0x26 & 0xFF);
	public static byte SET_LEFT_HOR_SCROLL = (byte)(0x27 & 0xFF);
	public static byte SET_VER_AND_RIGHT_HOR_SCROLL = (byte)(0x29 & 0xFF);
	public static byte SET_VER_AND_LEFT_HOR_SCROLL = (byte)(0x2A & 0xFF);
	public static byte SET_VER_SCROLL_AREA = (byte)(0xA3 & 0xFF);
	
	
	//these commands constitute a complete action on their own
	
	public static byte MAP_ADDRESS_0_TO_SEG0 = (byte)(0xA0 & 0xFF);
	public static byte MAP_ADDRESS_127_TO_SEG0 = (byte)(0xA1 & 0xFF);
	
	public static byte USE_DISPLAY_RAM = (byte)(0xA4 & 0xFF);
	public static byte IGNORE_DISPLAY_RAM = (byte)(0xA5 & 0xFF);
	
	public static byte TURN_ON_INVERSE_DISPLAY =  (byte)(0xA6 & 0xFF);
	public static byte TURN_OFF_INVERSE_DISPLAY = (byte)(0xA7 & 0xFF);
	
	public static byte PUT_DISPLAY_TO_SLEEP = (byte)(0xAE & 0xFF);
	public static byte WAKE_DISPLAY = (byte)(0xAF & 0xFF);
	
	public static byte SET_COM_OUTPUT_SCAN_FWD = (byte)(0xC0 & 0xFF);
	public static byte SET_COM_OUTPUT_SCAN_BKWD = (byte)(0xC8 & 0xFF);
	
	public static byte DEACTIVATE_SCROLL = (byte)(0x2E & 0xFF);
	public static byte ACTIVATE_SCROLL = (byte)(0x2F & 0xFF);
	
	public static byte NOP = (byte)(0xE3 & 0xFF);
}
