package com.ociweb.iot.grove.four_digit_display;

public class FourDigitDisplayCommand {
	public static final int INIT = -1;
	public static final int SET_BRIGHTNESS = 0b1000_0000_0000_0000;
	public static final int DISPLAY_ON = -2;
	public static final int DISPLAY_OFF = - 3;
	public static final int PRINT_FOUR_DIGITS = 0;
}
