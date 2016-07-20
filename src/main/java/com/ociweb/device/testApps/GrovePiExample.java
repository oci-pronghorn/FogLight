package com.ociweb.device.testApps;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;

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
	private static int[] sign = {0,0};
	private static int[] exp = {0,0};
	private static int[] mantissa = {0,0};
	private static float[] vals = {0,0};

	public static void main(String[] args) {
		logger.info("Starting GrovePi example app.");

		System.out.println("#### Writing data ####");
		System.out.println("");
		byte[] readcmd = {0x01, 40, 0x07, 0, 0};
		//byte[] readcmd = {0x01, 7, 8, 0, 0};
		pinMode(14, 0);
		
		long start = System.currentTimeMillis();
		int count = 0;
		
		while(true){
//			count++;
//			analogRead(14);
//			if(count>1000){
//				System.out.println(System.currentTimeMillis()-start);
//				count = 0;
//			}
			
			i2c.write((byte) 0x04, readcmd, readcmd.length);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] read = {0,0,0,0,0,0,0,0,0};
			i2c.read((byte) 4, read, 9);
			
			
//			System.out.println("");
//			System.out.println(Arrays.toString(read));
//			System.out.println(ByteBuffer.wrap(read).order(ByteOrder.LITTLE_ENDIAN).getFloat(1)+", "+ByteBuffer.wrap(read).order(ByteOrder.LITTLE_ENDIAN).getFloat(5));
			count++;
			if(read[1] == -1 || read[5]==-1){
				System.out.println(count);
				count = 0;
			}
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			i2c.write((byte) 0x04, readcmd, readcmd.length);
//			try {
//				Thread.sleep(15);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			byte[] temp = {0,0,0};
//			i2c.read((byte) 4, temp, 3);
//			
//			System.out.println("");
//			System.out.println(Arrays.toString(temp));
//			System.out.println((((int)temp[1])<<8) | (0xFF&((int)temp[2])));
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			
			
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
