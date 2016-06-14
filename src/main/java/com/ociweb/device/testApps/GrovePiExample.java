package com.ociweb.device.testApps;

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Running the grove pi
 * 
 * i2c protocol found here: https://www.raspberrypi.org/forums/download/file.php?id=8272
 * 
 * @author alexherriott
 */
public class GrovePiExample {
	private static final Logger logger = LoggerFactory.getLogger(GrovePiExample.class);

	// Create a connection to the native Linux I2C lines.
	private static final I2CNativeLinuxBacking i2c = new I2CNativeLinuxBacking();

	// Address of board.
	public static final byte Grove_ADDR = 0x04;

	public static void main(String[] args) {
		logger.info("Starting GrovePi example app.");

		System.out.println("#### Writing data ####");
		System.out.println("");
		pinMode(14,0); //set A0 to input
		pinMode(3,0);  //set 3 to output
		pinMode(2,0);
		byte[] encoderDis = {0x01, 0x11, 0x00, 0x00, 0x00};
		i2c.write(Grove_ADDR, encoderDis);
		try {
			Thread.sleep(250);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		byte[] encoderEn = {0x01, 0x10, 0x00, 0x00, 0x00};
		pinMode(2, 0);
		pinMode(3, 0);
		i2c.write(Grove_ADDR, encoderEn);
		int response = 0;
		while (true) {
			
			
			response = encoderRead();

//			try{
//				Thread.sleep(250);
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
			// Sleep for a bit.
			//			try {
			//				Thread.sleep(250);
			//			} catch (Exception e) {
			//				logger.error(e.getMessage(), e);
			//			}

		}
	}

	static void digitalWrite(int pin, int val){
		byte[] message = {0x01, 0x02, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message);    
	}

	static byte digitalRead(int pin){ //0 is input, 1 is output
		byte[] message = {0x01, (byte) pin, 0x00, 0x00};
		i2c.write(Grove_ADDR, message);
		return i2c.read(Grove_ADDR, 1)[0];
	}

	static void pinMode(int pin, int val){
		byte[] message = {0x01, 0x05, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message); 
	}

	static void analogWrite(int pin, int val){
		byte[] message = {0x01, 0x04, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message); 
	}
	static int analogRead(int pin){
		byte[] message = {0x01, 0x03, (byte) pin, 0x00, 0x00};
		i2c.write(Grove_ADDR, message);
		byte[] ans = i2c.read(Grove_ADDR, 3);
		return ans[1]*256+((int)ans[2]&0xFF);
	}
	static int encoderRead(){ //As yet only returns 0 -1
		byte[] message = {0x01, 0x0B, 0x00, 0x00, 0x00};
		i2c.write(Grove_ADDR, message);
		byte[] ans = i2c.read(Grove_ADDR, 2);
		System.out.println(ans[0] + "  " + ans[1]);
		if(ans[0]>0){
			return ans[1];
		}else{
			return -1;
		}
	}
}
