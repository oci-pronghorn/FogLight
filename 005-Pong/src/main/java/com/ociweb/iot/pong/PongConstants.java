package com.ociweb.iot.pong;

public class PongConstants {

	public static final int Player1Con = 0;
	public static final int Player2Con = 2;

	public static final long GAME_STEP = 50;
	public static final int BALL_CHAR = 0;
	public static final int SPACE = 32;

	public static final int PADDLE_1_COL = 1;
	public static final int PADDLE_2_COL = 10;
	public static final int PADDLE_1_UP_CHAR = 1;
	public static final int PADDLE_1_DOWN_CHAR = 2;
	public static final int PADDLE_2_UP_CHAR = 3;
	public static final int PADDLE_2_DOWN_CHAR = 4;

	public static final int LEFT_LIMIT = PADDLE_1_COL*6+4;
	public static final int RIGHT_LIMIT = PADDLE_2_COL*6+3;
	public static final int UP_LIMIT = 1;
	public static final int DOWN_LIMIT = 16;


	public final static byte[] colLookup = {
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






	//	public static byte[] generateLeftPaddle(int col, int row){
	//		
	//	}

}
