package com.ociweb.iot.grove;

public class Grove_OLED_128x64_Constants {
	//commands
	
	//these commands set a value for a particular parameter (i.e. brightness) take up
	//a range of values (i.e. 0x00 through 0x0f). The base (lowest valued) command is listed as a consant here.
	
	public static int LOWER_COL_START_ADDRESS_PAGE_MODE = 0x00;//0x00 ~ 0x0f
	public static int HIGH_COL_START_ADDRESS_PAGE_MODE = 0x10; //0x10 ~ 0x1f
	public static int DISPLAY_START_LINE = 0x40; //40~7f
	public static int PAGE_START_ADDRESS_PAGE_MODE = 0xB0;//0xB0 ~ 0x B7
	
	//these commands require further input afterwards
	public static int SET_MEMORY = 0x20;
	public static int SET_COLUMN_ADDRESS = 0x21;
	public static int SET_PAGE_ADDRESS = 0x22;
	public static int SET_CONTRAST_CONTROL= 0x81;
	public static int SET_MUX_RATIO = 0xA8;
	public static int SET_EXT_INT_I_REF = 0xAD;
	public static int SET_DISPLAY_OFFSET = 0xD3;
	public static int SET_CLOCK_DIV_RATIO = 0xD5;
	public static int SET_PRECHARGE_PERIOD = 0xD9;
	public static int SET_COM_PINS_CONFIG = 0xDA;
	public static int SET_VCOM_DESELECT_LEVEL = 0xDB;
	
	
	//these commands constitute a complete action on their own
	
	public static int MAP_ADDRESS_0_TO_SEG0 = 0xA0;
	public static int MAP_ADDRESS_127_TO_SEG0 = 0xA1;
	
	public static int USE_DISPLAY_RAM = 0xA4;
	public static int IGNORE_DISPLAY_RAM = 0xA5;
	
	public static int TURN_ON_INVERSE_DISPLAY =  0xA6;
	public static int TURN_OFF_INVERSE_DISPLAY = 0xA7;
	
	public static int PUT_DISPLAY_TO_SLEEP = 0xAE;
	public static int WAKE_DISPLAY = 0xAF;
	
	public static int SET_COM_OUTPUT_SCAN_FWD = 0xC0;
	public static int SET_COM_OUTPUT_SCAN_BKWD = 0xC8;
	
	public static int NOP = 0xE3;
}
