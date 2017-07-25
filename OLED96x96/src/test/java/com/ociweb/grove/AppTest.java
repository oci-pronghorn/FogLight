package com.ociweb.grove;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

/**
 * Unit test for simple App.
 */
public class AppTest { 


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
	
	public void testImageSerialization(){
		/*
		try {
			int[][] map = null;
			map = ImageGenerator.convertToGrayScale(4, 0, 0, 96, 96);
			FileOutputStream fos = new FileOutputStream("/Users/ray/Documents/workspace/FogLight-Grove/OLED96x96/src/main/resources/oci.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		*/
	}
}
