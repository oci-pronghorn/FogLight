package com.ociweb.iot.grove.oled;


/**
 * 
 * @author Ray Lo, raylo@wustl.edu
 *
 */
public class SH1107G_Consts {
	public static final int DISPLAY_OFF =  0xAE;
	public static final int DISPLAY_ON = 0xAF;
	
	public static final int SET_D_CLOCK = 0xD5;
	public static final int SET_ROW = 0x20;
	public static final int SET_ROW_BASE_BYTE = 0xB0;
	public static final int SET_CONTRAST = 0x81;
	public static final int REMAP_SGMT = 0xA0;
	public static final int NORMAL_DISPLAY = 0xA6;
	public static final int ENTIRE_DISPLAY_ON = 0xA4;
	public static final int SET_EXT_VCC = 0xAD;
	public static final int SET_COMMON_SCAN_DIR = 0xC0;
	public static final int SET_PHASE_LENGTH = 0xD9;
	public static final int SET_VCOMH_VOLTAGE = 0xDB;
	public static final int SET_HORIZONTAL = 0xC8;
	public static final int SET_VERTICAL = 0xC0;

}
