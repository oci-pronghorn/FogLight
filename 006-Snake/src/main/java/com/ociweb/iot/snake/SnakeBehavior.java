package com.ociweb.iot.snake;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.grove.oled.OLED_128x64_Transducer;
import com.ociweb.iot.grove.thumb_joystick.ThumbJoystickListener;

import static com.ociweb.iot.grove.oled.OLEDTwig.*;
import static com.ociweb.iot.maker.FogRuntime.I2C_WRITER;

import java.util.Random;

public class SnakeBehavior implements Behavior, TimeListener, StartupListener, ThumbJoystickListener{
	private final OLED_128x64_Transducer screen;

	private boolean inGame = true;
	private int length = 3;
	private Direction curDir = Direction.Right;
	
	private static final int xLength = 128;
	private static final int yLength = 64;
	private static final int totalPixels = xLength * yLength;
	private int[] x = new int[totalPixels];
	private int[] y = new int[totalPixels];
	private int[][] gameState = new int[yLength][xLength];
	private int foodX;
	private int foodY;
	private final static int middleValue = 509;

	SnakeBehavior(FogRuntime runtime){
		screen = OLED_128x64.newTransducer(runtime.newCommandChannel(I2C_WRITER,2000));
	}

	@Override
	public void startup() {
		startGame();
	}

	private void startGame(){
		System.out.println("Game Started");
		screen.clear();
		inGame = true;
		for(int i = 0;i<totalPixels;i++){
			x = new int[totalPixels];
			y = new int[totalPixels];
		}

		x[0] = 2;
		x[1] = 1;
		x[2] = 0;
		y[0] = 0;
		y[1] = 0;
		y[2] = 0;
		length = 3;
		curDir = Direction.Right;
		drawSnake(x,y);
		makeFood();
	}
	
	
	private void setPixel(int row, int col, boolean high){
		gameState[row][col] = high? 1:0;
	}
	private void move(Direction dir){

		setPixel(y[length-1],x[length-1], false);

		for(int i =length -1;i>0;i--){
			x[i] =x[i-1];
			y[i] =y[i-1];
		}

		switch(dir){
		case Up:
			y[0] = y[0] - 1;
			break;
		case Down:
			y[0] = y[0] + 1;
			break;
		case Left:
			x[0] = x[0] - 1;
			break;
		case Right:
			x[0] = x[0] + 1;
			break;
		default:
			break;
		}
		if(x[0] == foodX && y[0] == foodY){
			length += 1;
			x[length - 1] = x[length-2];
			y[length - 1] = y[length-2];
			makeFood();
		}        
		drawSnake(x,y);

		if((!isInRange(x[0],y[0])) || isSelfEating()){
			gameOver();
		}

	}

	private void gameOver(){
		inGame = false;
		System.out.println("Game Over. Your Score: "+length);
		for(int i = 1;i<length;i++){
			setPixel(y[i],x[i],true);
		}
		startGame();
	}

	public boolean isInRange(int x,int y){
		return (x >= 0 && x <= xLength) && (y >= 0 && y <= yLength);
	}

	public boolean isSelfEating(){
		for(int i = 1;i<length;i++){
			if(x[0] == x[i] && y[0] == y[i]){
				return true;
			}
		}
		return false;
	}

	private void printToScreen(){
		screen.display(gameState);
	}

	private void makeFood(){
		Random rn = new Random();
		boolean food = true;
		while(food){
			food = false;
			foodX = rn.nextInt(xLength);
			foodY = rn.nextInt(yLength);
			for(int i = 0;i<length;i++){
				if(foodX == x[i] && foodY == y[i]){
					food = true;
				}
			}
		}
		setPixel(foodY, foodX, true);
	}

	private void drawSnake(int[] x,int[] y){
		setPixel(y[0], x[0], true);
		for(int i = 1;i<length;i++){
			setPixel(y[i],x[i],true);
		}
	}

	@Override
	public void timeEvent(long l, int i) {
		if(inGame){
			move(curDir);
			printToScreen();
		}
	}

	
	@Override
	public void joystickValues(int x, int y) {
		int xDiff = x - middleValue;
		int yDiff = y - middleValue;
		
		boolean xIsMoreExtreme = true;
		int moreExtremeDiff = xDiff;
		if (Math.abs(xDiff) < Math.abs(yDiff)){
			xIsMoreExtreme = false;
			moreExtremeDiff = yDiff;
		}
		if (Math.abs(moreExtremeDiff) > 150){
			if (xIsMoreExtreme){
				if (moreExtremeDiff > 0){
					curDir = Direction.Right;
				}
				else {
					curDir = Direction.Left;
				}
			}
			else {
				if (moreExtremeDiff > 0){
					curDir = Direction.Up;
				}
				else {
					curDir = Direction.Down;
				}
			}
		}
	}
	
	enum Direction{Up, Down,Left,Right};

}
