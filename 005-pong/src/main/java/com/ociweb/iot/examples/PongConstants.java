package com.ociweb.iot.examples;

import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.lcd_rgb.Grove_LCD_RGB_Patterns;

public class PongConstants {

	public static final Port Player1Con = A1;
	public static final Port Player2Con = A2;
	public static final Port ButtonCon = D2;

	public static final int PADDLE_1_COL = 1;
	public static final int PADDLE_2_COL = 8;
	public static final int SCORE_COL = 5;

	public static final int LEFT_LIMIT = PADDLE_1_COL*6+4;
	public static final int RIGHT_LIMIT = PADDLE_2_COL*6+1;
	public static final int UP_LIMIT = 1;
	public static final int DOWN_LIMIT = 16;
	public static final int CHAR_HEIGHT = 9;
	public static final int CHAR_WIDTH = 6;
	
	public static final int BALL_CHAR = 0;
	public static final int PADDLE_1_UP_CHAR = 1;
	public static final int PADDLE_1_DOWN_CHAR = 2;
	public static final int PADDLE_2_UP_CHAR = 3;
	public static final int PADDLE_2_DOWN_CHAR = 4;
	public static final int BACK_CHAR = 5;


	public final static byte[] rowLookup = {
			0b10000,
			0b11000,
			0b01100,
			0b00110,
			0b00011,
			0b00001
	};

	public final static long TITLE_TIME = 5000;
	public final static byte[][] waveStates = {
			{
				0b01101,
				0b01101,
				0b11011,
				0b11011,
				0b10110,
				0b10110,
				0b01101,
				0b01101
			},
			{
				0b10110,
				0b10110,
				0b01101,
				0b01101,
				0b11011,
				0b11011,
				0b10110,
				0b10110
			},
			{
				0b11011,
				0b11011,
				0b10110,
				0b10110,
				0b01101,
				0b01101,
				0b11011,
				0b11011
			}
	};
	
	public final static byte[] backgroundChar = Grove_LCD_RGB_Patterns.dithered;
	
	public final static int[][] backGround = {
			{0, 0},
			{0, 1},
			{9, 0},
			{9, 1},
			{10,0},
			{10,1},
			{11,0},
			{12,0},
			{13,0},
			{14,0},
			{14,1},
			{15,0},
			{15,1}
	};
	


}
