package com.ociweb.iot.grove.OLED;

public class SSD1327_Consts {

	//commands
	public static final int ADDRESS = 0x3C;
	public static final int COMMAND_MODE = 0x80;
	public static final int DATA_MODE = 0x40;
	public static final int DISPLAY_OFF = 0xAE;
	public static final int DISPLAY_ON = 0xAF;
	public static final int NORMAL_DISPLAY = 0xA4;
	public static final int INVERSE_DISPLAY = 0xA7;
	public static final int ACTIVATE_SCROLL_CMD = 0x2F;
	public static final int DEACTIVATE_SCROLL_CMD = 0x2E;
	public static final int SET_CONTRAST_LEVEL_CMD = 0x81;
	
	public static final int REMAP = 0xA0;
	public static final int HORIZONTAL = 0x42;
	public static final int VERTICAL = 0x46;
	
	public static final int SET_ROW_ADDRESS = 0x75;
	public static final int SET_COL_ADDRESS = 0x15;
	

	
	public static final int MCU = 0xFD;
	public static final int UNLOCK_CMD_ENTERING = 0x12;
	
	public static final int SET_MULTIPLEX_RATIO = 0xA8;
	
	public static final int SET_DISPLAY_START_LINE = 0xA1;
	public static final int SET_DISPLAY_OFFSET = 0xA2;
	
	public static final int SET_VDD_INTERNAL = 0xAB;
	public static final int SET_PHASE_LENGTH = 0x81;
	public static final int SET_CLOCK_DIV_RATIO = 0xB3;
	public static final int SET_PRECHARGE_VOLTAGE_AND_VCOMH =0xBC;
	public static final int SET_VCOMH = 0xBE;
	public static final int SET_SECOND_PRECHARGE_PERIOD = 0xB6;
	public static final int ENABLE_SECOND_PRECHARGE_AND_INTERNAL_VSL = 0x62;
	
	
	//Each row corresponds to an ASCII character startign at ASCII 32.
	//Each byte corresponds to 8-pixels. When communicating with the chip on the display, a nible corressponds to
	//to a pixel, not a bit. Hence, we need to convert this bit array to an "nibble" array in the OLED_96x96 object layer.
	

}
