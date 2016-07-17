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
	private static final I2CNativeLinuxBacking i2c = new I2CNativeLinuxBacking((byte) 1);

	// Address of board.
	public static final byte Grove_ADDR = 0x04;

	public static void main(String[] args) {
		logger.info("Starting GrovePi example app.");

		System.out.println("#### Writing data ####");
		System.out.println("");
		
		pinMode(6,0);  //set 3 to output
		
		while(true){
			digitalWrite(6, 1);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			digitalWrite(6, 0);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	static void digitalWrite(int pin, int val){
		byte[] message = {0x01, 0x02, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message, message.length);    
	}

	static byte digitalRead(int pin){ //0 is input, 1 is output
		byte[] message = {0x01, 0x01, (byte) pin, 0x00, 0x00};
		i2c.write(Grove_ADDR, message, message.length);
		return i2c.read(Grove_ADDR,new byte[1], 1)[0];
	}

	static void pinMode(int pin, int val){
		byte[] message = {0x01, 0x05, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message, message.length); 
	}

	static void analogWrite(int pin, int val){
		byte[] message = {0x01, 0x04, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message, message.length); 
	}
	static int analogRead(int pin){
		byte[] message = {0x01, 0x03, (byte) pin, 0x00, 0x00};
		i2c.write(Grove_ADDR, message, message.length);
		byte[] ans = i2c.read(Grove_ADDR,new byte[3], 3);
		return ans[1]*256+((int)ans[2]&0xFF);
	}
	static int encoderRead(){ //As yet only returns 0 -1
		byte[] message = {0x01, 0x0B, 0x00, 0x00, 0x00};
		i2c.write(Grove_ADDR, message, message.length);
		byte[] ans = i2c.read(Grove_ADDR,new byte[2], 2);
		System.out.println(ans[0] + "  " + ans[1]);
		if(ans[0]>0){
			return ans[1];
		}else{
			return -1;
		}
	}
}
