package com.ociweb.iot.grove.four_digit_display;

import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.IODeviceFacade;
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
	
	private static final int bit_duration = 15;
	
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

	public static final int GROVE_TM1637_ADDRESS = 0x4;
	public static final int GROVE_TM1637_INIT = 70; // in decimal as provided by Grove github
	public static final int GROVE_TM1637_SET_BRIGHTNESS = 71;
	public static final int GROVE_TM1637_DISPLAY_WITH_LEADING_ZEROES = 72;
	public static final int GROVE_TM1637_DISPLAY_WITHOUT_LEADING_ZEROES = 73;
	public static final int GROVE_TM1637_SET_INDV_DIGIT = 74;
	public static final int GROVE_TM1637_SET_INDV_SEGMENT = 75;
	public static final int GROVE_TM1637_SET_SCOREBOARD = 76;
	public static final int GROVE_TM1637_DISPLAY_ANALOG_READ_ = 77;
	public static final int GROVE_TM1637_DISPLAY_ON = 78;
	public static final int GROVE_TM1637_DISPLAY_OFF = 79;
	
	
	
	
	//bitmap for the digits
	public static final byte [] digit_font = 
		{
				0x3f,0x06,0x5b,0x4f, 0x66,0x6d,0x7d,0x07, 0x7f
		};

	public static final Grove_FourDigitDisplay instance = new Grove_FourDigitDisplay();

	private Grove_FourDigitDisplay(){
	}
	
	public static FourDigitDisplayFacade newFacade(FogCommandChannel ch, Port p){
		return new FourDigitDisplayFacade(ch, p);
	}
	
	public static boolean init(FogCommandChannel ch, Port p){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_INIT);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	public static boolean setBrightness(FogCommandChannel ch, Port p, int brightness){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_SET_BRIGHTNESS);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(brightness & 0x07); //brightness is only valid 0 through 7
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	public static boolean printDigitAt(FogCommandChannel ch, Port p, int index, int digit){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_SET_INDV_DIGIT);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(index); //index is only valid 0 through 3
		i2cPayloadWriter.writeByte(digit); //digit only 0 through 9
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	public static boolean printFourDigitsWithColon(FogCommandChannel ch, Port p, int leftPair, int rightPair){
		if (!ch.i2cIsReady()){
			System.out.println("Why though");
			return false;
		}
		System.out.println("Writing I2C");
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_SET_SCOREBOARD);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(leftPair); //brightness is only valid 0 through 3
		i2cPayloadWriter.writeByte(rightPair); //digit only 0 through 9
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	
	public static boolean displayOn(FogCommandChannel ch, Port p){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_DISPLAY_ON);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	public static boolean displayOff(FogCommandChannel ch, Port p){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(GROVE_TM1637_ADDRESS);
		i2cPayloadWriter.writeByte(GROVE_TM1637_DISPLAY_OFF);
		i2cPayloadWriter.writeByte(p.port);
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		i2cPayloadWriter.writeByte(0x00);//dummy byte
		ch.i2cCommandClose();
		ch.i2cFlushBatch();	
		return true;
	}
	
	
	
//	
//	/**
//	 * Prints a digit at a given location with colon off
//	 * @param target {@link FogCommandChannel}
//	 * @param digit 0-9 number to be printed
//	 * @param position ranges from 0 to 3 on the 4-digit display
//	 */
//	public static void printDigitAt(FogCommandChannel target, int digit, int position){
//		printDigitAt(target, digit, position, false);
//	}
//	/**
//	 * Prints a digit at a given location
//	 * @param target {@link FogCommandChannel}
//	 * @param digit 0-9 number to be printed
//	 * @param position ranges from 0 to 3 on the 4-digit display
//	 * @param colon_on true value turns on the colon, false value turns it off
//	 */
//	public static void printDigitAt(FogCommandChannel target, int digit, int position, boolean colon_on){
//
//		drawBitmapAt(target, (byte)digit_font[digit], position, colon_on);
//
//	}
//
//	/**
//	 * Prints a custom character according to the bitmap supplied at the given position
//	 * @param target {@link FogCommandChannel} 
//	 * @param b byte with the bitmap to be printed
//	 * @param position ranges from 0 to 3 on the 4-digit display
//	 * @param colon_on true value turns on the colon, false value turns it off
//	 */
//	public static void drawBitmapAt(FogCommandChannel target, byte b, int position, boolean colon_on){
//		//go into fixed address mode
//		start(target);//5
//		sendByte(target, (byte)ADDR_FIXED);//29
//		stop(target);//6
//
//		b = (byte) (b | COLON_DISPLAY_OFF);
//
//		if (colon_on){
//			b = (byte) (b | COLON_DISPLAY_ON);
//		}
//		//send position of the digit (0 through 4) and the bitmap
//		start(target); //5
//		sendByte(target, (byte)(position | ADDR_CMD));//29
//		sendByte(target, (byte)(b));//29
//		stop(target); //6
//		start(target);//5
//		sendByte(target,(byte)(DISPLAY_ON | BRIGHTNESS));//29
//		stop(target);//6
//	}
//	
//	/**
//	 * Clear out the 4-digit display
//	 * @param target {@link FogCommandChannel}
//	 */
//	public static void clearDisplay(FogCommandChannel target){
//	}
//
//	//Address commands all start with 0xC0
//	public static final int ADDR_CMD = 0xC0;
//	/**
//	 * starts the TM1637 targetip's listening; data is changed from high to low while clock is high
//	 * @param target
//	 */
//	public static void start(FogCommandChannel target){
//		//takes up 5 commands
//		System.out.println("Starting message"); //FIXME: remove after debugging
//		target.setValueAndBlock(CLOCK, false, bit_duration);
//		target.setValueAndBlock(DATA, true, bit_duration * 2);
//		target.setValueAndBlock(CLOCK, true, bit_duration * 2);
//		target.setValueAndBlock(DATA, false, bit_duration * 2);
//		target.setValueAndBlock(CLOCK, false, bit_duration);
//		
//		//takes 4 * bit_duration
//	}
//	/**
//	 * ends the TM1637 targetip's listening; data is from low to high while clock is high
//	 * @param target
//	 */
//	public static void stop(FogCommandChannel target){
//		System.out.println("Stop"); //FIXME: remove after debugging
//		//takes up 6 commands
//		target.setValueAndBlock(CLOCK, false, bit_duration);
//		target.setValueAndBlock(DATA, false, bit_duration * 2);
//		target.setValueAndBlock(CLOCK, true, bit_duration * 2);
//		target.setValueAndBlock(DATA, true, bit_duration * 2);
//		target.setValueAndBlock(CLOCK, false, bit_duration * 2);
//		target.setValueAndBlock(DATA, false, bit_duration);
//		
//	}
//
//	/**
//	 * sends a byte and ignores the ack back bit by bit with bit-banging
//	 * blocking has to be longer sometimes because the API does not allow for blocking between ports, so the
//	 * blocking of the two ports are syncopated to gurantee ordering.
//	 * @param target
//	 * @param b
//	 */
//	private static boolean sendByte(FogCommandChannel target, byte b){
//		//takes up 29 commands
//		if (! target.setValue(DATA, false)) {
//			return false;
//		}
//		
//		if (! target.setValueAndBlock(CLOCK, false, bit_duration)){
//			return false;
//		}
//		
//		System.out.println("Sending byte: 0b" + Integer.toBinaryString(b&0xFF)); //FIXME: remove after debugging
//		for (int i = 7; i >= 0; i--){
//			if (!target.setValueAndBlock(DATA, highBitAt(b,i), bit_duration)){
//				return false;
//			}
//			System.out.print(highBitAt(b,i));
//			if (!target.setValueAndBlock(CLOCK,true, bit_duration)){
//				return false;
//			}
//			if (!target.setValueAndBlock(CLOCK, false, bit_duration)){
//				return false;
//			}
//			
//		}
//		System.out.println();
//		
//		//Cycling the clock once to ignore the ack
//		if (!target.setValueAndBlock(CLOCK, true, bit_duration)){
//			return false;
//		}
//		if (!target.setValueAndBlock(CLOCK, false,bit_duration)){
//			return false;
//		}
//		if  (!target.setValue(DATA, false)){
//			return false;
//		}
//		return true;
//		
//		// takes bit_duration * 27
//	}
//
//	private static boolean highBitAt(byte b, int pos){
//		return (b & (0x01 << pos))!=0; 
//	}

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

	@Override
	public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}

}


