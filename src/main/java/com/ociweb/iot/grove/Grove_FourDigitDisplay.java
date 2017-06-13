package com.ociweb.iot.grove;

import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.CommandChannel;


/**
 * Utility class to interface with the Grove Four Digit display.
 * @author Ray Lo
 *
 */
public class Grove_FourDigitDisplay {

	//TODO: Hardcoded ports for now. The connection needs to be moved.
	public static final Port  CLOCK = D2;
	public static final Port  DATA = D3;

	public static final int BRIGHTNESS = 15; // 0 to 15

	//commands: we can bit-wise or these commands to combine them

	//Data commands:
	//B7 and B6 are always "0 1" for data commands
	//since all of the data commands have a 4 in them as bytes to start out with, there is 
	//no need to bitwise 'or' with DATA_CMD again.
	public static final int DATA_CMD = 0x40;

	//B5 and B4 should always be 0

	//B3 (test mode setting for internal)
	public static final int NORM_MODE = 0x40;
	public static final int TEST_MODE = 0x48;

	//B2:
	public static final int ADDR_AUTO = 0x40;
	public static final int ADDR_FIXED = 0x44;

	//B1 and B0
	public static final int WRITE_TO_REGISTER = 0x40;
	public static final int READ_KEY_SCAN_DATA = 0x42;


	//Data input
	public static final int COLON_DISPLAY_OFF = 0x00;
	public static final int COLON_DISPLAY_ON = 0x80;

	//Display commands are signaled by 0x80 in the start.
	//Brightness goes from 0 to 15 and can simply be added/ or bit-wise 'or'ed onto
	//the DISPLAY_ON command byte.

	public static final int DISPLAY_ON = 0x88;
	public static final int DISPLAY_OFF = 0x80;

	//bitmap for the digits
	public static final int [] digit_font = 
		{
				0x3f,0x06,0x5b,0x4f, 0x66,0x6d,0x7d,0x07, 0x7f
		};


	/**
	 * Prints a digit at a given location with colon off
	 * @param target {@link CommandChannel} initialized via {@link #begin(CommandChannel)}
	 * @param digit 0-9 number to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 */
	public static void printDigitAt(CommandChannel target, int digit, int position){
		printDigitAt(target, digit, position, false);
	}
	/**
	 * Prints a digit at a given location
	 * @param target {@link CommandChannel} initialized via {@link #begin(CommandChannel)}
	 * @param digit 0-9 number to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 * @param colon_on true value turns on the colon, false value turns it off
	 */
	public static void printDigitAt(CommandChannel target, int digit, int position, boolean colon_on){

		drawBitmapAt(target, (byte)digit_font[digit], position, colon_on);

	}

	/**
	 * Prints a custom character according to the bitmap supplied at the given position
	 * @param target {@link CommandChannel} initialized via {@link #begin(CommandChannel)}
	 * @param b byte with the bitmap to be printed
	 * @param position ranges from 0 to 3 on the 4-digit display
	 * @param colon_on true value turns on the colon, false value turns it off
	 */
	public static void drawBitmapAt(CommandChannel target, byte b, int position, boolean colon_on){
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
	 * @param target {@link CommandChannel} initialized via {@link #begin(CommandChannel)}
	 */
	public static void clearDisplay(CommandChannel target){
	}

	//Address commands all start with 0xC0
	public static final int ADDR_CMD = 0xC0;
	/**
	 * starts the TM1637 targetip's listening; data is changed from high to low while clock is high
	 * @param target
	 */
	private static void start(CommandChannel target){
		target.setValue(DATA, true);
		target.setValueAndBlock(CLOCK, true,1);
		target.setValueAndBlock(DATA, false,1);
		target.setValueAndBlock(CLOCK, false,1);
	}
	/**
	 * ends the TM1637 targetip's listening; data is from low to high while clock is high
	 * @param target
	 */
	private static void stop(CommandChannel target){
		target.setValue(DATA, false);
		target.setValueAndBlock(CLOCK, true,1);
		target.setValueAndBlock(DATA, true,1);
		target.setValueAndBlock(CLOCK, false,1);

	}

	/**
	 * sends a byte and ignores the ack back bit by bit with bit-banging
	 * @param target
	 * @param b
	 */
	private static void sendByte(CommandChannel target, byte b){
		target.setValueAndBlock(CLOCK,false,1);
		for (int i = 7; i >= 0; i--){
			target.setValueAndBlock(DATA, highBitAt(b,i), 1);
			target.setValueAndBlock(CLOCK, true,1);
			target.setValueAndBlock(CLOCK,false,1);
		}
		//ignoring ack, TODO: Ideally we would read the ack and return it
		target.setValueAndBlock(CLOCK, true,1);
		target.setValue(CLOCK, false);
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

}


