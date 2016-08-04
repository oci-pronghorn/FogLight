package com.ociweb.iot.grove;

/*
 * Constants derived from Seeed LCD with RGB backlight data sheet here:
 * http://www.seeedstudio.com/wiki/images/0/03/JHD1214Y_YG_1.0.pdf
 *
 */
public class Grove_LCD_RGB_Constants {

	// Device I2C Adress (note this only uses the lower 7 bits)
		public static int LCD_ADDRESS  =   (0x7c>>1); //  11 1110  0x3E
		public static final int RGB_ADDRESS  =   (0xc4>>1); // 110 0010  0x62


		// color define 
		public static final int WHITE       =    0;
		public static final int RED         =    1;
		public static final int GREEN       =    2;
		public static final int BLUE        =    3;

		public static final int REG_RED     =    0x04;        // pwm2
		public static final int REG_GREEN   =    0x03;        // pwm1
		public static final int REG_BLUE    =    0x02;        // pwm0

		public static final int REG_MODE1    =   0x00;
		public static final int REG_MODE2    =   0x01;
		public static final int REG_OUTPUT   =   0x08;

		// commands
		public static final int LCD_CLEARDISPLAY   =0x01;
		public static final int LCD_RETURNHOME     =0x02;
		public static final int LCD_ENTRYMODESET   =0x04;
		public static final int LCD_DISPLAYCONTROL =0x08;
		public static final int LCD_CURSORSHIFT    =0x10;
		public static final int LCD_FUNCTIONSET    =0x20;
		public static final int LCD_TWO_LINES      =0x28;
		public static final int LCD_SETCGRAMADDR   =0x40;
		public static final int LCD_SETDDRAMADDR   =0x80;

		// flags for display entry mode
		public static final int LCD_ENTRYRIGHT          =0x00;
		public static final int LCD_ENTRYLEFT           =0x02;
		public static final int LCD_ENTRYSHIFTINCREMENT =0x01;
		public static final int LCD_ENTRYSHIFTDECREMENT =0x00;

		// flags for display on/off control
		public static final int LCD_DISPLAYON  =0x04;
		public static final int LCD_DISPLAYOFF =0x00;
		public static final int LCD_CURSORON   =0x02;
		public static final int LCD_CURSOROFF  =0x00;
		public static final int LCD_BLINKON    =0x01;
		public static final int LCD_BLINKOFF   =0x00;

		

		// flags for display/cursor shift
		public static final int LCD_DISPLAYMOVE =0x08;
		public static final int LCD_CURSORMOVE  =0x00;
		public static final int LCD_MOVERIGHT   =0x04;
		public static final int LCD_MOVELEFT    =0x00;

		// flags for function set
		public static final int LCD_8BITMODE =0x10;
		public static final int LCD_4BITMODE =0x00;
		public static final int LCD_2LINE =0x08;
		public static final int LCD_1LINE =0x00;
		public static final int LCD_5x10DOTS =0x04;
		public static final int LCD_5x8DOTS =0x00;
		
		public static boolean isStarted = false;

	    protected static final long MS_TO_NS = 1_000_000;
	    
	    public static final int SCREEN_CLEAR_DELAY = 1_530_000;
	    public static final int CURSOR_RETURN_DELAY = 1_530_000;
	    public static final int INPUT_SET_DELAY = 39_000;
	    public static final int DISPLAY_SWITCH_DELAY = 39_000;
	    public static final int SHIFT_DELAY = 39_000;
	    public static final int FUNCTION_SET_DELAY = 39_000;
	    public static final int CGRAM_SET_DELAY = 39_000;
	    public static final int DDRAM_SET_DELAY = 39_000;
	    public static final int DDRAM_WRITE_DELAY = 43_000;

}
