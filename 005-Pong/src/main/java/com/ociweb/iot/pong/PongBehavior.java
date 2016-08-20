package com.ociweb.iot.pong;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.StateChangeListener;
import com.ociweb.iot.maker.TimeListener;

import java.util.Arrays;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;

public class PongBehavior implements StartupListener, TimeListener, AnalogListener, StateChangeListener {

	private CommandChannel pongChannel;

	private int ballRow = 0; //row in pixels
	private int ballCol =7*PongConstants.CHAR_WIDTH;
	private int currentBallRow = 0; //current row in characters
	private int currentBallCol = 7;
	private int ballRowDelta = 1; //direction ball is traveling in pixels
	private int ballColDelta = 1;
	private byte[] ballMap = new byte[8];

	private int player1Loc = 0;
	private int player2Loc = 0;
	private byte[] paddleMap = new byte[8];
	private byte[] paddleAndBallMap = new byte[8];
	
	private long startTime = -1;
	private int waveState = 0; 
	
	private long scoreTime = -1;
	private int player1Score = 0;
	private int player2Score = 0;
	
	private enum GameState { //TODO: pull out as own file.
		startUp, playing, score
	};
	private GameState gameState = GameState.startUp; //TODO: using state listener this is not needed.

	public PongBehavior(DeviceRuntime runtime) {
		this.pongChannel = runtime.newCommandChannel(); 
	}	

	@Override
	public void startup() {
		
		Grove_LCD_RGB.begin(pongChannel); //TODO: should we not do this for the makers??? 
		
		//TODO: record the time we started here.
		
		Grove_LCD_RGB.commandForColor(pongChannel, 0, 255, 0);
		
		

		System.out.println("setup complete");
		
	}

	@Override
	public void timeEvent(long time) {
		switch(gameState){
			case startUp:
				doStartup(time);
				break;
				
			case playing:
				doPlay();
				break;
				
			case score:
				doScore(time);			
				break;
		}
	}

	private void doScore(long time) {
		if(scoreTime == -1){
			Grove_LCD_RGB.writeCharSequence(pongChannel, player1Score+"-"+player2Score, 11, 1);
			scoreTime = time;
		}
		
		int secondsLeft = (int) (3-(time-scoreTime)/1000);
		Grove_LCD_RGB.writeCharSequence(pongChannel,  Integer.toString(secondsLeft), 6, 0); 
		
		if(time-scoreTime>3000){
			scoreTime = -1;
			Grove_LCD_RGB.writeChar(pongChannel, (byte)' ', 6, 0);
			resetBallPos();
			//drawBackground();
			drawPaddles();
			gameState = GameState.playing;
		}
	}

	private void doPlay() {
		//boundary detection
		// bounce off the top and bottom
		if(ballRow >= PongConstants.DOWN_LIMIT){
			ballRowDelta = -1;
		}else if(ballRow <= PongConstants.UP_LIMIT){
			ballRowDelta = 1;
		}
		//bounce off the paddles
		if(ballCol <=PongConstants.LEFT_LIMIT){
//			if((ballRow>=player1Loc) && (ballRow<=player1Loc+2)){
//				ballColDelta = 1;
//			}
//			else if((ballRow>=player1Loc-1) && (ballRow<=player1Loc+3)){
//				ballColDelta = 1;
//				ballRowDelta = -ballRowDelta;
//			}
//			else{
//				gameState = GameState.score;
//				player2Score++;
//			}
			ballColDelta = 1;
		}
		if(ballCol <=PongConstants.RIGHT_LIMIT){
//			if((ballRow>=player2Loc) && (ballRow<=player2Loc+2)){
//				ballColDelta = -1;
//			}
//			else if((ballRow>=player2Loc-1) && (ballRow<=player2Loc+3)){
//				ballColDelta = -1;
//				ballRowDelta = -ballRowDelta;
//			}
//			else{
//				gameState = GameState.score;
//				player1Score++;
//			}
			ballColDelta = -1;
		}
		ballRow += ballRowDelta;
		ballCol += ballColDelta;
		

		//calculate ball character location
		final int oldBallRow = currentBallRow;   //TODO: may need better names
		final int oldBallCol = currentBallCol;
		currentBallRow = ballRow/PongConstants.CHAR_HEIGHT;
		currentBallCol = ballCol/PongConstants.CHAR_WIDTH;

		//clear old ball char location
		if((currentBallRow != oldBallRow || currentBallCol != oldBallCol) && (oldBallCol != PongConstants.PADDLE_1_COL)){
			Grove_LCD_RGB.writeChar(pongChannel, (byte)' ', oldBallCol, oldBallRow); //writing a space to the old location is cheaper than using clearDisplay()
		}
		
		//set chars to char maps
		if(currentBallCol == PongConstants.PADDLE_1_COL){ //if ball exists inside paddle1
			if(currentBallRow == 0){
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player1Loc));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(player1Loc-PongConstants.CHAR_HEIGHT));
			}else{
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(player1Loc));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player1Loc-PongConstants.CHAR_HEIGHT));
			}
		}
		else if(currentBallCol == PongConstants.PADDLE_2_COL){ //if ball exists inside paddle2
			if(currentBallRow == 0){
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_UP_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player2Loc));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_DOWN_CHAR, generatePaddleMap(player2Loc-PongConstants.CHAR_HEIGHT));
			}else{
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_UP_CHAR, generatePaddleMap(player2Loc));
				Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_DOWN_CHAR, generateBallAndPaddleMap(ballCol, ballRow, player2Loc-PongConstants.CHAR_HEIGHT));
			}
		}
		else{
			Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.BALL_CHAR, generateBallMap(ballCol, ballRow));
			Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(player1Loc));
			Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(player1Loc-PongConstants.CHAR_HEIGHT));
		}

		//write new ball char location
		if((currentBallRow != oldBallRow || currentBallCol != oldBallCol) && currentBallCol != PongConstants.PADDLE_1_COL){
				Grove_LCD_RGB.writeChar(pongChannel, PongConstants.BALL_CHAR, currentBallCol, currentBallRow);
		}
	}

	private void doStartup(long time) {
		if(startTime == -1){
			startTime = time;
			drawTitle();
		}
		
		Grove_LCD_RGB.setCustomChar(pongChannel, 0, PongConstants.waveStates[waveState/4]); // create waves
		waveState = (waveState+1)%12;

		if(time - startTime >= PongConstants.TITLE_TIME){ //TODO: consider exitTime constant, for score as well.
			Grove_LCD_RGB.clearDisplay(pongChannel);
			resetBallPos();
			gameState = GameState.score; //draw the score and count down
		}
	}

	
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		if(port == PongConstants.Player1Con){
			player1Loc = (value-4)/68; //maps 1024 to 0-15
		}else if(port == PongConstants.Player2Con){
			player2Loc = (value-4)/68;
		}
	}


	private byte[] generateBallMap(int col, int row){
		col = col%PongConstants.CHAR_WIDTH; 
		row = row%PongConstants.CHAR_HEIGHT;
		for (int i = 0; i < ballMap.length; i++){
			ballMap[i] = (i == row-1 || i == row)  ? PongConstants.rowLookup[col] : 0; //TODO: test this???
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
//		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(0));
//		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(-2));
//		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_UP_CHAR, generatePaddleMap(0));
//		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_DOWN_CHAR, generatePaddleMap(-2));
		
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, PongConstants.PADDLE_1_COL, 0);
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, PongConstants.PADDLE_1_COL, 1);
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_2_UP_CHAR, PongConstants.PADDLE_2_COL, 0);
		Grove_LCD_RGB.writeChar(pongChannel, PongConstants.PADDLE_2_DOWN_CHAR, PongConstants.PADDLE_2_COL, 1);
	}
	
	private void resetBallPos() {
		ballRow = 0+ (int)(Math.random()*5);
		ballCol = 5*PongConstants.CHAR_WIDTH;
	}

	private void drawTitle() {
		byte[] charIdxs = new byte[32]; //32 chars on screen
		Arrays.fill(charIdxs, (byte)0);  //fill screen with first custom character
		for (int i = 0; i < 6; i++) {
			charIdxs[i+5] = (byte)" Pong ".charAt(i); //write title in middle of screen
			charIdxs[i+21] = (byte)' ';
		}
		Grove_LCD_RGB.writeMultipleChars(pongChannel, charIdxs, 0, 0); //actually write the chars to the screen
	}
	
	private void drawBackground(){
		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_UP_CHAR, generatePaddleMap(0));
		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_1_DOWN_CHAR, generatePaddleMap(-2));
		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_UP_CHAR, generatePaddleMap(0));
		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.PADDLE_2_DOWN_CHAR, generatePaddleMap(-2));
		
		Grove_LCD_RGB.setCustomChar(pongChannel, PongConstants.BACK_CHAR, PongConstants.backgroundChar);
		for (int i = 0; i < PongConstants.backGround.length; i++) {
			Grove_LCD_RGB.writeChar(pongChannel, PongConstants.BACK_CHAR, PongConstants.backGround[i][0], PongConstants.backGround[i][1]);
		}
	}

	@Override
	public void stateChange(Enum oldState, Enum newState) {
		// TODO Auto-generated method stub
		
	}

	
	


}
