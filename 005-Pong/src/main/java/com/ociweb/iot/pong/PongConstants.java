package com.ociweb.iot.pong;

public class PongConstants {

	public static final int Player1Con = 0;
	public static final int Player2Con = 2;
	
	public static final long GAME_STEP = 50;
	public static final int BALL_CHAR = 0;
	public static final int SPACE = 32;
	
	public static final int LEFT_LIMIT = 2*6;
	public static final int RIGHT_LIMIT = 10*6-1;
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
	
	
	
	
	
	
//	public static byte[] generateLeftPaddle(int col, int row){
//		
//	}

}
