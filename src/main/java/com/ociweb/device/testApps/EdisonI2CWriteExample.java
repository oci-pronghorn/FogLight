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
public class EdisonI2CWriteExample {
	private static final Logger logger = LoggerFactory.getLogger(EdisonI2CWriteExample.class);

	// Create a connection to the native Linux I2C lines.
	private static final I2CNativeLinuxBacking i2c = new I2CNativeLinuxBacking();

	// Address of board.
	public static final byte Grove_ADDR = 0x04;

	public static void main(String[] args) {
		logger.info("Starting GrovePi example app.");

		System.out.println("#### Writing data ####");
		System.out.println("");
		byte addr = 0x62;
		
		byte [] message0 = {0,0};
		byte [] message1 = {1,0};
		byte [] message2 = {(byte)0x08, (byte) 0xaa};
		byte [] message3 = {(byte)4, (byte)0xc8};
		i2c.write(addr, message0);
		i2c.write(addr, message1);
		i2c.write(addr, message2);
		i2c.write(addr, message3);
	}

	static void digitalWrite(int pin, int val){
		byte[] message = {0x01, 0x02, (byte) pin, (byte) val, 0x00};
		i2c.write(Grove_ADDR, message);    
	}

	static byte digitalRead(int pin){ //0 is input, 1 is output
		byte[] message = {0x01, 0x01, (byte) pin, 0x00, 0x00};
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
