package com.ociweb.iot.pong;

public class PongConstants {

	
	public static final long GAME_STEP = 500;
	public static final int BALL_CHAR = 0;
	
	public static final int LEFT_LIMIT = 1;
	public static final int RIGHT_LIMIT = 16*6;
	public static final int UP_LIMIT = 1;
	public static final int DOWN_LIMIT = 18;
	
	
	private static byte[] ballMap = new byte[8];
	/**
	 * generates 2x2 ball. top left corner is at location. location starts at row and column 1. row and column 0 are in the space around characters
	 * @param col
	 * @param row
	 * @return
	 */
	public static byte[] generateBall(int col, int row){
		col = 5-(col%6);
		int tempCol = (1<<col)%32;
		row = row%9;
		for (int i = 0; i < ballMap.length; i++){
			if(i == row-1 || i == row){
				ballMap[i] = (byte)tempCol;
			}else{
				ballMap[i] = 0;
			}
		}
		return ballMap;
	}
	
//	public static byte[] generateLeftPaddle(int col, int row){
//		
//	}

}
