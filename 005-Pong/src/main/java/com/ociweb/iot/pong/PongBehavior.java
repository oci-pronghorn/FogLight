package com.ociweb.pronghorn.iot.pong;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;

public class PongBehavior implements StartupListener, TimeListener, AnalogListener {
 
    private static final int PAUSE = 500;
	
	private CommandChannel pongChannel;
	
	private int ballRow = 0; //row in pixels
	private int ballCol = 0;
	private int currentBallRow = 0; //current row in characters
	private int currentBallCol = 0;
	private int ballRowDelta = 1; //direction ball is traveling in pixels
	private int ballColDelta = 1;
	private long prevStep = 0;
	
	public PongBehavior(DeviceRuntime runtime) {
		pongChannel = runtime.newCommandChannel(); 
	}	
	
	@Override
	public void startup() {
		Grove_LCD_RGB.begin(pongChannel);  
	}

	@Override
	public void timeEvent(long time) {
		if(time>prevStep + PongConstants.GAME_STEP){
			gameStep();
			prevStep = time;
		}	
		paintLCD(pongChannel);
	}

	
	@Override
	public void analogEvent(int connector, long time, int average, int value) {
		// TODO Auto-generated method stub
		
	}
	
	private void paintLCD(CommandChannel target){
		if(currentBallRow != ballRow/9 || currentBallCol != ballCol/6){
			currentBallRow = ballRow/9;
			currentBallCol = ballCol/6;
			Grove_LCD_RGB.clearDisplay(target);
			Grove_LCD_RGB.setCursor(target, currentBallCol, currentBallRow);
		}
		Grove_LCD_RGB.writeChar(target, PongConstants.BALL_CHAR);	
	};
	
	private void gameStep(){
		if(ballCol >= PongConstants.RIGHT_LIMIT || ballCol <= PongConstants.LEFT_LIMIT){
			ballColDelta = -ballColDelta;
		}
		if(ballRow >= PongConstants.DOWN_LIMIT || ballRow <= PongConstants.UP_LIMIT){
			ballRowDelta = -ballRowDelta;
		}
		ballRow += ballRowDelta;
		ballCol += ballColDelta;
		Grove_LCD_RGB
	}

}
