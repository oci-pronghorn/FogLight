package com.ociweb.grove;

import java.io.IOException;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 

	/////////////////
	// IF YOU'D LIKE TO GENERATE YOUR OWN IMAGE VIA A GUI, TURN THIS BOOLEAN ON.
	/////////////////
	private boolean askToGenerateImage = false;

	@Test
	public void testApp()
	{
		FogRuntime runtime = FogRuntime.test(new OLED96x96());	    	
		NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
		TestHardware hardware = (TestHardware)runtime.getHardware();

		scheduler.startup();


		int iterations = 10;
		while (--iterations >= 0) {

			scheduler.run();

			//test application here

		}


		scheduler.shutdown();

	}
	@Test
	public void testInit(){

	}
	@Test
	public void testImageSerialization(){
		if (askToGenerateImage){
			try {
				int[][] map = null;
				map = ImageGenerator.convertToGrayScale(4, 0, 0, 96, 96);
				
				//Uncomment if you would like to generate an image and serialize it to disk.
				//FileOutputStream fos = new FileOutputStream("/YOUR/PATH/HERE");
				//ObjectOutputStream oos = new ObjectOutputStream(fos);
				//oos.writeObject(map);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
}
