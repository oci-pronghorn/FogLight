package com.ociweb.iot.hardware.impl.test;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.util.Appendables;

public class TestI2CBacking implements I2CBacking{

	public static final int MAX_TEST_SIZE = 2048;
	public static final int MAX_ADDRESS   = 127;

	public static final int MAX_BACK_BITS =  7;//127 MESSAGES
	public static final int MAX_BACK_SIZE =  1<<MAX_BACK_BITS;
	public static final int MAX_BACK_MASK =  MAX_BACK_SIZE-1;

	private static final Logger logger = LoggerFactory.getLogger(TestI2CBacking.class);

	private boolean configured = false;

	private long[]   lastWriteTime;
	private byte[]   lastWriteAddress;
	private byte[][] lastWriteData;
	private int[]    lastWriteLength;
	private int      lastWriteIdx;
	private int      lastWriteCount;

	public byte[][] responses;
	public int[] responseLengths;


	public TestI2CBacking() {

		lastWriteTime    = new long[MAX_BACK_SIZE];
		lastWriteAddress = new byte[MAX_BACK_SIZE];
		lastWriteData    = new byte[MAX_BACK_SIZE][];
		int i = MAX_BACK_SIZE;
		while (--i>=0) {
			lastWriteData[i] = new byte[MAX_TEST_SIZE];
		}

		lastWriteLength  = new int[MAX_BACK_SIZE];

		responses = new byte[MAX_ADDRESS][];
		responseLengths = new int[MAX_ADDRESS];
	}


	public void setValueToRead(byte address, byte[] data, int length) {
		responses[address] = data;
		responseLengths[address] = length;
	}


	@Override
	public TestI2CBacking configure(byte bus) throws IllegalStateException {
		if (configured) {
			throw new IllegalStateException();
		} else {
			configured = true;
		}

		return this;
	}

	boolean reportedTestHardwareRequest = false;
	
	@Override
	public byte[] read(byte address, byte[] target, int length) throws IllegalStateException {
		if (!configured) {
			throw new IllegalStateException();
		}

		if (null != responses[address]) {    		
			System.arraycopy(responses[address], 0, target, 0, Math.min(length, responseLengths[address]));
		} else {
			if (!reportedTestHardwareRequest) {
				//for this case the developer did not provide test data
				logger.warn("Test hardware was asked for I2C read on address {} but nothing was prepared to be sent back. call hardware.setI2CValueToRead((byte){},data,len) to prevent this warning.", address,address);
				reportedTestHardwareRequest = true;
			}
		}
		return target;

	}

	boolean newLineNeeded = false;

	@Override
	public boolean write(byte address, byte[] message, int length) throws IllegalStateException {
		if (!configured) {
			throw new IllegalStateException();
		}
		assert(length<=message.length);
		lastWriteCount++;
		lastWriteTime[lastWriteIdx] = System.currentTimeMillis();
		lastWriteAddress[lastWriteIdx] = address;
		System.arraycopy(message, 0, lastWriteData[lastWriteIdx], 0, length);
		lastWriteLength[lastWriteIdx] = length;

		lastWriteIdx = (1+lastWriteIdx) & MAX_BACK_MASK;

		consoleSimulationLCD(address, message, length);

		return true;
	}


	protected void consoleSimulationLCD(byte address, byte[] message, int length) {
		assert(length<=message.length);
		
		if (length==0) {
			
			Appendables.appendHexDigits(System.out.append("Zero length message payload sent to address "),address).append("\n");
						
		} else if (62==address && '@'==message[0]) {

			for(int i = 1; i<length; i++) {

				byte b = message[i];
				if (b<=0xF && (b!=0xA && b!=0xD)) { //line feed and cr should not be shown as numbers
					Appendables.appendHexDigits(System.out, b).append(',');
				} else {
					System.out.append((char)b);
				}

			}


			newLineNeeded = true;
		} else {
			if (62==address && 2==length && -128==message[0] && -64==message[1] ) {
				System.out.println(); //new line message
				newLineNeeded=false;
			} else {
				if (newLineNeeded) {
					System.out.println(); 
					newLineNeeded=false;
				}
/*
				System.out.print("                         I2C Write to Addr:"+address+"   [");
				//Appendables.appendArray(System.out, '[', message, 0, Integer.MAX_VALUE, ']', length).append('\n');
				for (int i = 0; i < length; i++){
					Appendables.appendFixedHexDigits(System.out, message[i] & 0xFF, 8).append(", ");
				}
				System.out.println("]");
*/
			}
		}
	}

	public void clearWriteCount() {
		lastWriteCount = 0;
	}


	public int getWriteCount() {
		return lastWriteCount;
	}


	public <A extends Appendable>void outputLastI2CWrite(A target, int backCount) {

		try {
			int previous = MAX_BACK_MASK & ((lastWriteIdx + MAX_BACK_SIZE) - backCount);
			Appendables.appendHexDigits(target, this.lastWriteAddress[previous]).append(" ");
			Appendables.appendArray(target, '[', this.lastWriteData[previous], ']', this.lastWriteLength[previous]);        
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}



}
