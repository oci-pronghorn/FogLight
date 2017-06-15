package com.ociweb.iot.grove;

import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;


/**
 * Utility class to interface with the Grove Four Digit display.
 * @author Ray Lo
 *
 */
public class Grove_FourDigitDisplay implements IODevice{

	//TODO: Hardcoded ports for now. The connection needs to be moved.
	public static final Port  CLOCK = D5;
	public static final Port  DATA = D6;

	public static final byte BRIGHTNESS = 15; // 0 to 15
	
	private static final int bit_duration = 1000;
	
	//commands: we can bit-wise or these commands to combine them

	//Data commands:
	//B7 and B6 are always "0 1" for data commands
	//since all of the data commands have a 4 in them as bytes to start out with, there is 
	//no need to bitwise 'or' with DATA_CMD again.
	public static final byte DATA_CMD = 0x40 & 0xFF;

	//B5 and B4 should always be 0

	//B3 (test mode setting for internal)
	public static final byte NORM_MODE = 0x40 & 0xFF;
	public static final byte TEST_MODE = 0x48 & 0xFF;

	//B2:
	public static final byte ADDR_AUTO = 0x40 & 0xFF;
	public static final byte ADDR_FIXED = 0x44 & 0xFF;

	//B1 and B0
	public static final byte WRITE_TO_REGISTER = 0x40 & 0xFF;
	public static final byte READ_KEY_SCAN_DATA = 0x42 & 0xFF;


	//Data input
	public static final byte COLON_DISPLAY_OFF = 0x00 & 0xFF;
	public static final byte COLON_DISPLAY_ON = (byte) (0x80 & 0xFF);

	//Display commands are signaled by 0x80 in the start.
	//Brightness goes from 0 to 15 and can simply be added/ or bit-wise 'or'ed onto
	//the DISPLAY_ON command byte.

	public static final byte DISPLAY_ON = (byte) (0x88 & 0xFF);
	public static final byte DISPLAY_OFF = (byte)(0x80 & 0xFF);

	//bitmap for the digits
	public static final byte [] digit_font = 
		{
				0x3f,0x06,0x5b,0x4f, 0x66,0x6d,0x7d,0x07, 0x7f
		};


	/**
	 * Prints a digit at a given location with colon off
	 * @param target {@link FogCommandChannel}
	 * @param digit 0-9 number to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 */
	public static void printDigitAt(FogCommandChannel target, int digit, int position){
		printDigitAt(target, digit, position, false);
	}
	/**
	 * Prints a digit at a given location
	 * @param target {@link FogCommandChannel}
	 * @param digit 0-9 number to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 * @param colon_on true value turns on the colon, false value turns it off
	 */
	public static void printDigitAt(FogCommandChannel target, int digit, int position, boolean colon_on){

		drawBitmapAt(target, (byte)digit_font[digit], position, colon_on);

	}

	/**
	 * Prints a custom character according to the bitmap supplied at the given position
	 * @param target {@link FogCommandChannel} 
	 * @param b byte with the bitmap to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 * @param colon_on true value turns on the colon, false value turns it off
	 */
	public static void drawBitmapAt(FogCommandChannel target, byte b, int position, boolean colon_on){
		//go into fixed address mode
		start(target);
		sendByte(target, (byte)ADDR_FIXED);
		stop(target);

		b = (byte) (b | COLON_DISPLAY_OFF);

		if (colon_on){
			b = (byte) (b | COLON_DISPLAY_ON);
		}
		//send position of the digit (0 through 4) and the bitmap
		start(target);
		sendByte(target, (byte)(position | ADDR_CMD));
		sendByte(target, (byte)(b));
		stop(target);
		start(target);
		sendByte(target,(byte)(DISPLAY_ON | BRIGHTNESS));
		stop(target);
	}
	
	/**
	 * Clear out the 4-digit display
	 * @param target {@link FogCommandChannel}
	 */
	public static void clearDisplay(FogCommandChannel target){
	}

	//Address commands all start with 0xC0
	public static final int ADDR_CMD = 0xC0;
	/**
	 * starts the TM1637 targetip's listening; data is changed from high to low while clock is high
	 * @param target
	 */
	public static void start(FogCommandChannel target){
		System.out.println("Starting message"); //FIXME: remove after debugging
		target.setValueAndBlock(CLOCK, false, bit_duration);
		target.setValueAndBlock(DATA, true, bit_duration * 2);
		target.setValueAndBlock(CLOCK, true, bit_duration * 2);
		target.setValueAndBlock(DATA, false, bit_duration * 2);
		target.setValueAndBlock(CLOCK, false, bit_duration);
		
		//takes 4 * bit_duration
	}
	/**
	 * ends the TM1637 targetip's listening; data is from low to high while clock is high
	 * @param target
	 */
	public static void stop(FogCommandChannel target){
		System.out.println("Stopping message"); //FIXME: remove after debugging
		target.setValueAndBlock(CLOCK, false, bit_duration);
		target.setValueAndBlock(DATA, false, bit_duration * 2);
		target.setValueAndBlock(CLOCK, true, bit_duration * 2);
		target.setValueAndBlock(DATA, true, bit_duration * 2);
		target.setValueAndBlock(CLOCK, false, bit_duration * 2);
		target.setValueAndBlock(DATA, false, bit_duration);
		
		//takes 5 * bit_duraiton
	}

	/**
	 * sends a byte and ignores the ack back bit by bit with bit-banging
	 * blocking has to be longer sometimes because the API does not allow for blocking between ports, so the
	 * blocking of the two ports are syncopated to gurantee ordering.
	 * @param target
	 * @param b
	 */
	private static void sendByte(FogCommandChannel target, byte b){
		target.setValueAndBlock(DATA, false, bit_duration);
		System.out.println("Sending byte: 0b" + Integer.toBinaryString(b&0xFF)); //FIXME: remove after debugging
		for (int i = 7; i >= 0; i--){
			target.setValueAndBlock(CLOCK, false, bit_duration*2);
			target.setValueAndBlock(CLOCK,true, bit_duration);
			target.setValueAndBlock(DATA, highBitAt(b,i), bit_duration * 3);
		}
		target.setValueAndBlock(CLOCK, false, bit_duration);
		//ignoring ack, TODO: Ideally we would read the ack and return it
		target.setValueAndBlock(CLOCK, true, bit_duration);
		target.setValueAndBlock(CLOCK, false,bit_duration);
		target.block(DATA, bit_duration * 2);
		
		// takes bit_duration * 27
	}

	private static boolean highBitAt(byte b, int pos){
		return (b & (0x01 << pos))!=0; 
	}

	/**
	 * 
	 * @param b the byte to be read
	 * @param pos 0 ~ 7 with 7 being the leftmost bit
	 * @return 1 if the bit is high, 0 otherwise
	 */
	private static int bitAt(byte b, int pos){
		return (b & (0x01 << pos)) ;
	}
	@Override
	public int response() {
		return 20;
	}
	@Override
	public int scanDelay() {
		return 20;
	}
	@Override
	public boolean isInput() {
		return false;
	}
	@Override
	public boolean isOutput() {
		return true;
	}
	@Override
	public boolean isPWM() {
		return false;
	}
	@Override
	public int range() {
		return 2;
	}
	@Override
	public I2CConnection getI2CConnection() {
		return null;
	}
	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return true;
	}
	@Override
	public int pinsUsed() {
		return 2;
	}

}


