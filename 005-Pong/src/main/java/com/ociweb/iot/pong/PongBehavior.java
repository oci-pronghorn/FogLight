package com.ociweb.iot.pong;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;

public class PongBehavior implements StartupListener, TimeListener, AnalogListener, I2CListener {
 
    private static final int PAUSE = 500;
	
	private CommandChannel pongChannel;
	
	private int ballRow = 0; //row in pixels
	private int ballCol = 0;
	private int currentBallRow = 0; //current row in characters
	private int currentBallCol = 0;
	private int ballRowDelta = 1; //direction ball is traveling in pixels
	private int ballColDelta = 1;
	private byte[] ballMap = new byte[8];
	
	private int player1Loc = 0;
	private byte[] paddleMap = new byte[8];
	
	public PongBehavior(DeviceRuntime runtime) {
		this.pongChannel = runtime.newCommandChannel(); 
	}	
	
	@Override
	public void startup() {
		Grove_LCD_RGB.begin(pongChannel);  
		Grove_LCD_RGB.commandForColor(pongChannel, 0, 255, 0);
		Grove_LCD_RGB.setCursor(pongChannel, 1, 0);
		Grove_LCD_RGB.writeChar(pongChannel, 1);
		Grove_LCD_RGB.setCursor(pongChannel, 1, 1);
		Grove_LCD_RGB.writeChar(pongChannel, 2);
		System.out.println("setup complete");
	}

	@Override
	public void timeEvent(long time) {
		
		gameStep(pongChannel);
		paintLCD(pongChannel);
	}
	
	@Override
	public void analogEvent(int connector, long time, int average, int value) {
		player1Loc = (value-2)/73; //value 0-14
	}
	
	@Override
	public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
		// TODO Auto-generated method stub
		
	}
	
	private void paintLCD(CommandChannel target){
		//Paint the ball
		if(currentBallRow != ballRow/9 || currentBallCol != ballCol/6){
			Grove_LCD_RGB.setCursor(target, currentBallCol, currentBallRow);
			Grove_LCD_RGB.writeChar(target, PongConstants.SPACE);
			
			currentBallRow = ballRow/9;
			currentBallCol = ballCol/6;
			//Grove_LCD_RGB.clearDisplay(target);
			
			Grove_LCD_RGB.setCursor(target, currentBallCol, currentBallRow);
			Grove_LCD_RGB.writeChar(target, PongConstants.BALL_CHAR);	
		}
		
		//Paint the paddle
//		Grove_LCD_RGB.setCustomChar(target, 1, generatePaddleMap(player1Loc));
//		Grove_LCD_RGB.setCustomChar(target, 2, generatePaddleMap(player1Loc-9));
	};
	
	private void gameStep(CommandChannel target){
		if(ballCol >= PongConstants.RIGHT_LIMIT){
			ballColDelta = -1;
		}else if(ballCol <= PongConstants.LEFT_LIMIT){
			ballColDelta = 1;
		}
		if(ballRow >= PongConstants.DOWN_LIMIT){
			ballRowDelta = -1;
		}else if(ballRow <= PongConstants.UP_LIMIT){
			ballRowDelta = 1;
		}
		ballRow += ballRowDelta;
		ballCol += ballColDelta;
		Grove_LCD_RGB.setCustomChar(target, PongConstants.BALL_CHAR, generateBallMap(ballCol%6, ballRow%9));
	}



	private byte[] generateBallMap(int col, int row){
		col = col%6;
		row = row%9;
		for (int i = 0; i < ballMap.length; i++){
			ballMap[i] = (i == row-1 || i == row) ? PongConstants.colLookup[col] : 0;
		}
		return ballMap;
	}
	
	private byte[] generatePaddleMap(int row){
		byte temp = 0b00100;
		for (int i = 0; i < paddleMap.length; i++) {
			paddleMap[i] = (i >= row && i <= row +2) ? temp : 0;
		}
		return paddleMap;
	}
	

}
