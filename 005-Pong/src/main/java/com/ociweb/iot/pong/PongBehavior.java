package com.ociweb.iot.pong;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;

import java.util.Arrays;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;

public class PongBehavior implements StartupListener, TimeListener, AnalogListener {

	private CommandChannel pongChannel;

	private int ballRow = 0; //row in pixels
	private int ballCol =7*6;
	private int currentBallRow = 0; //current row in characters
	private int currentBallCol = 7;
	private int ballRowDelta = 1; //direction ball is traveling in pixels
	private int ballColDelta = 1;
	private byte[] ballMap = new byte[8];

	private int player1Loc = 0;
	private byte[] paddleMap = new byte[8];
	private byte[] paddleAndBallMap = new byte[8];
	
	private long startTime = -1;
	private int waveState = 0; 
	
	private enum GameState {
		startUp, playing, score
	};
	private GameState gameState = GameState.startUp;

	public PongBehavior(DeviceRuntime runtime) {
		this.pongChannel = runtime.newCommandChannel(); 
	}	

	@Override
	public void startup() {
		Grove_LCD_RGB.begin(pongChannel);  
		Grove_LCD_RGB.commandForColor(pongChannel, 0, 255, 0);

		System.out.println("setup complete");
		
	}

	@Override
	public void timeEvent(long time) {
		switch(gameState){
		case startUp:
			if(startTime == -1){
				startTime = time;
				byte[] charIdxs = new byte[32];
				Arrays.fill(charIdxs, (byte)0);
				for (int i = 0; i < 6; i++) {
					charIdxs[i+5] = (byte)" Pong ".toCharArray()[i];
					charIdxs[i+21] = (byte)"      ".toCharArray()[i];
				}
				Grove_LCD_RGB.writeMultipleChars(pongChannel, charIdxs, 0, 0);
			}
			
			Grove_LCD_RGB.setCustomChar(pongChannel, 0, PongConstants.waveStates[waveState/4]);
			waveState = (waveState+1)%12;
		
			if(time - startTime >= PongConstants.TITLE_TIME){
				Grove_LCD_RGB.clearDisplay(pongChannel);
				drawPaddles();
				gameState = GameState.playing;
			}
			break;
		case playing:
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

			//calculate ball character location
			int oldBallRow = currentBallRow;
			int oldBallCol = currentBallCol;
			currentBallRow = ballRow/9;
			currentBallCol = ballCol/6;

			//clear old ball char location
			if((currentBallRow != oldBallRow || currentBallCol != oldBallCol) && oldBallCol != PongConstants.PADDLE_1_COL){
				Grove_LCD_RGB.setCursor(pongChannel, oldBallCol, oldBallRow);
				Grove_LCD_RGB.writeChar(pongChannel, PongConstants.SPACE); //writing a space to the old location is cheaper than using clearDisplay()
			}
			
			//set chars to char maps
			if(currentBallCol == PongConstants.PADDLE_1_COL){
				if(currentBallRow == 0){
					Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player1Loc));
					Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(player1Loc-9));
				}else{
					Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(player1Loc));
					Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player1Loc-9));
				}
			} //TODO: add the right side
			else{
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.BALL_CHAR, generateBallMap(ballCol, ballRow));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(player1Loc));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(player1Loc-9));
			}

			//write new ball char location
			if((currentBallRow != oldBallRow || currentBallCol != oldBallCol) && currentBallCol != PongConstants.PADDLE_1_COL){
					Grove_LCD_RGB.setCursor(pongChannel, currentBallCol, currentBallRow);
					Grove_LCD_RGB.writeChar(pongChannel, PongConstants.BALL_CHAR);
			}
			break;
		case score:
			
			break;
		}
		//motion of the ball
		
		
	}

	@Override
	public void analogEvent(int connector, long time, long durationMillis, int average, int value) {
		player1Loc = (value-4)/68; //value 0-15
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

	private byte[] generateBallAndPaddleMap(int ballCol, int ballRow, int paddleRow){
		ballMap = generateBallMap(ballCol, ballRow);
		paddleMap = generatePaddleMap(paddleRow);
		for (int i = 0; i < ballMap.length; i++) {
			paddleAndBallMap[i] = (byte) (ballMap[i] | paddleMap[i]);
		}
		return paddleAndBallMap;
	}
	
	private void drawPaddles(){
		Grove_LCD_RGB.setCustomChar(pongChannel, 1, generatePaddleMap(0));
		Grove_LCD_RGB.setCustomChar(pongChannel, 2, generatePaddleMap(-2));
		Grove_LCD_RGB.setCursor(pongChannel, PongConstants.PADDLE_1_COL, 0);
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR);
		Grove_LCD_RGB.setCursor(pongChannel, PongConstants.PADDLE_1_COL, 1);
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR);
	}
	


}
