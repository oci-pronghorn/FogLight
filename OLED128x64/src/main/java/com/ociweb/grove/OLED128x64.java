package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.grove.Grove_OLED_128x64;
import static com.ociweb.iot.grove.Grove_OLED_128x64_Constants.*;
import com.ociweb.iot.maker.*;


public class OLED128x64 implements FogApp
{


	private boolean gameOfLife = true;
	@Override
	public void declareConnections(Hardware c) {
		c.useI2C();
		if (gameOfLife){
			c.setTriggerRate(1000);
		}
		//c.enableTelemetry(true);
		//TODO: give warning message if trigger rate was not set and time listener is used
	}

	public static void main (String[] args){
		FogRuntime.run(new OLED128x64());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel(0,20000);
		if (gameOfLife){
			GameOfLife game = new GameOfLife(ch);
			runtime.addListener(game);
		}
		else 
		{
			runtime.addStartupListener(()->{
				//    		int[][] raw = new int[64][128];
				//    		try {
				//				BufferedImage i = ImageIO.read(OLED128x64.class.getResource("snoopy.png"));
				//				for (int row = 0; row < 64; row ++){
				//					for (int col = 0 ; col < 128; col ++){
				//						int [] rgb = null;
				//						int [] stuff = i.getRaster().getPixel(col, row, rgb);
				//						for (int in: stuff){
				//							System.out.print(in + " ,");
				//						}
				//						System.out.println();
				//					}
				//				}
				//			
				//				
				//			} catch (IOException e) {
				//				// TODO Auto-generated catch block
				//				e.printStackTrace();
				//			}
				//			System.out.println("Printing string");
				Grove_OLED_128x64.init(ch);
				Grove_OLED_128x64.clear(ch);
				Grove_OLED_128x64.drawBitmapInPageMode(ch, testLogo);

				//Grove_OLED_128x64.displayImage(ch, testImage);
			});
			runtime.addTimeListener((time,iteration) ->{
				//    		int brightness = (int)(Math.sin(time/(1000.0*Math.PI)) * 128 + 128);
				//    		Grove_OLED_128x64.cleanClear(ch);
				//    		Grove_OLED_128x64.drawBitmap(ch, testLogo);
				//    		
				//    		if (iteration %2 == 0){
				//    			Grove_OLED_128x64.turnOffInverseDisplay(ch);
				//    		} else {
				//    			Grove_OLED_128x64.turnOnInverseDisplay(ch);
				//    		}
			});
		}
	}

}
