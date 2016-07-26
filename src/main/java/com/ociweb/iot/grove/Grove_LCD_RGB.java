package com.ociweb.iot.grove;

import java.io.IOException;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 * TODO: This class probably needs to be renamed and moved; it's now both a simple API and collection of constants.
 *
 * @author Nathan Tippy
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class Grove_LCD_RGB implements IODevice{ 

	// Device I2C Adress (note this only uses the lower 7 bits)
	public static int LCD_ADDRESS  =   (0x7c>>1); //  11 1110  0x3E
	public static final int RGB_ADDRESS  =   (0xc4>>1); // 110 0010  0x62


	// color define 
	public static final int WHITE       =    0;
	public static final int RED         =    1;
	public static final int GREEN       =    2;
	public static final int BLUE        =    3;

	public static final int REG_RED     =    0x04;        // pwm2
	public static final int REG_GREEN   =    0x03;        // pwm1
	public static final int REG_BLUE    =    0x02;        // pwm0

	public static final int REG_MODE1    =   0x00;
	public static final int REG_MODE2    =   0x01;
	public static final int REG_OUTPUT   =   0x08;

	// commands
	public static final int LCD_CLEARDISPLAY   =0x01;
	public static final int LCD_RETURNHOME     =0x02;
	public static final int LCD_ENTRYMODESET   =0x04;
	public static final int LCD_DISPLAYCONTROL =0x08;
	public static final int LCD_CURSORSHIFT    =0x10;
	public static final int LCD_FUNCTIONSET    =0x20;
	public static final int LCD_TWO_LINES      =0x28;
	public static final int LCD_SETCGRAMADDR   =0x40;
	public static final int LCD_SETDDRAMADDR   =0x80;

	// flags for display entry mode
	public static final int LCD_ENTRYRIGHT          =0x00;
	public static final int LCD_ENTRYLEFT           =0x02;
	public static final int LCD_ENTRYSHIFTINCREMENT =0x01;
	public static final int LCD_ENTRYSHIFTDECREMENT =0x00;

	// flags for display on/off control
	public static final int LCD_DISPLAYON  =0x04;
	public static final int LCD_DISPLAYOFF =0x00;
	public static final int LCD_CURSORON   =0x02;
	public static final int LCD_CURSOROFF  =0x00;
	public static final int LCD_BLINKON    =0x01;
	public static final int LCD_BLINKOFF   =0x00;

	// Current LCD_DISPLAYCONTROL states
	private static int LCD_DISPLAY =LCD_DISPLAYON;
	private static int LCD_CURSOR =LCD_CURSOROFF;
	private static int LCD_BLINK =LCD_BLINKOFF;

	// flags for display/cursor shift
	public static final int LCD_DISPLAYMOVE =0x08;
	public static final int LCD_CURSORMOVE  =0x00;
	public static final int LCD_MOVERIGHT   =0x04;
	public static final int LCD_MOVELEFT    =0x00;

	// flags for function set
	public static final int LCD_8BITMODE =0x10;
	public static final int LCD_4BITMODE =0x00;
	public static final int LCD_2LINE =0x08;
	public static final int LCD_1LINE =0x00;
	public static final int LCD_5x10DOTS =0x04;
	public static final int LCD_5x8DOTS =0x00;


	/**
	 * <pre>
	 * Creates a complete byte array that will set the text and color of a Grove RGB
	 * LCD display when passed to a {@link com.ociweb.pronghorn.stage.test.ByteArrayProducerStage}
	 * which is using chunk sizes of 3 and is being piped to a {@link I2CStage}.
	 *
	 * TODO: This function is currently causing the last letter of the text to be dropped
	 *       when displayed on the Grove RGB LCD; there's currently a work-around that
	 *       simply appens a space to the incoming text variable, but it's hackish
	 *       and should be looked into more...
	 *
	 * @param text String to display on the Grove RGB LCD.
	 * @param r 0-255 value for the Red color.
	 * @param g 0-255 value for the Green color.
	 * @param b 0-255 value for the Blue color.
	 *
	 * @return Formatted byte array which can be passed directly to a
	 *         {@link com.ociweb.pronghorn.stage.test.ByteArrayProducerStage}.
	 * </pre>
	 */

	private static int index = 0;
	public static boolean displayDemo(CommandChannel target){
		switch(index){
		case(0):
			commandForDisplay(target, true);
		commandForBlink(target, true);
		commandForTextAndColor(target, "1", 255, 0, 0);
		break;
		case(1):
			commandForTextAndColor(target, "2", 0, 255, 0);
		break;
		case(2):
			commandForDisplay(target, false);
		commandForTextAndColor(target, "3", 0, 255, 255);
		break;
		case(3):
			commandForTextAndColor(target, "4", 255, 0, 255);
		break;
		case(4):
			commandForDisplay(target, true);
		break;
		case(5):
			commandForTextAndColor(target, "5", 0, 0, 255);
		break;
		}
		index = (index+1)%6;

		return true;
	}
	

	public static boolean commandForTextAndColor(CommandChannel target, String text, int r, int g, int b) {
		if (!target.i2cIsReady()) {
			return false;
		}

		showRGBColor(target, r, g, b);
		showTwoLineText(target, text);
		target.i2cFlushBatch();
		return true;
	}
	public static boolean commandForColor(CommandChannel target, int r, int g, int b) {

		if (!target.i2cIsReady()) {
			return false;
		}

		showRGBColor(target, r, g, b);

		target.i2cFlushBatch();
		return true;
	}
	public static boolean commandForText(CommandChannel target, String text) {

		if (!target.i2cIsReady()) {
			return false;
		}

		showTwoLineText(target, text);        
		target.i2cFlushBatch();

		return true;
	}

	public static boolean commandForDisplay (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_DISPLAY = LCD_DISPLAYON;
		}else{
			LCD_DISPLAY = LCD_DISPLAYOFF;
		}
		setDisplayControl(target);  
		target.i2cDelay((Grove_LCD_RGB.LCD_ADDRESS), 1);

		return true;
	}

	public static boolean commandForCursor (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_CURSOR = LCD_CURSORON;
		}else{
			LCD_CURSOR = LCD_CURSOROFF;
		}

		setDisplayControl(target); 
		target.i2cFlushBatch();

		return true;
	}

	public static boolean commandForBlink (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_BLINK = LCD_BLINKON;
		}else{
			LCD_BLINK = LCD_BLINKOFF;
		}

		setDisplayControl(target);        
		target.i2cFlushBatch();

		return true;
	}

	
	public static boolean setCustomChar(CommandChannel target, int location, byte chars[]){
		if (!target.i2cIsReady()) {
			return false;
		}
		location &= 0x7;
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), LCD_SETCGRAMADDR, location << 3);
		for (int i = 0; i < chars.length; i++) {
			writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), LCD_SETCGRAMADDR, chars[i]);
		}
		target.i2cDelay(LCD_ADDRESS, 1);
		target.i2cFlushBatch();
		return true;
	}

	private static void setDisplayControl(CommandChannel target){
		writeSingleByteToRegister(target, LCD_ADDRESS, LCD_SETDDRAMADDR, 
				LCD_DISPLAYCONTROL | LCD_DISPLAY | LCD_CURSOR | LCD_BLINK);
		target.i2cDelay((Grove_LCD_RGB.LCD_ADDRESS), 1);
	}

	private static void showRGBColor(CommandChannel target, int r, int g, int b) {
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 0, 0);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 1, 0);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 0x08, 0xaa);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 4, r);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 3, g);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.RGB_ADDRESS)), 2, b);
	}


	private static void displayClear(CommandChannel target) {
		//clear display
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.LCD_ADDRESS)), LCD_SETDDRAMADDR, LCD_CLEARDISPLAY);
		target.i2cDelay((Grove_LCD_RGB.LCD_ADDRESS), 2);

	}



	private static void showTwoLineText(CommandChannel target, String text) {

		//display on - no cursor
		//writeSingleByteToRegister(target, LCD_ADDRESS, LCD_SETDDRAMADDR, 
		//LCD_DISPLAYCONTROL | LCD_DISPLAYON);       

		//two lines
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.LCD_ADDRESS)), LCD_SETDDRAMADDR, LCD_TWO_LINES);        

		//clear display
		writeSingleByteToRegister(target, ((Grove_LCD_RGB.LCD_ADDRESS)), LCD_SETDDRAMADDR, LCD_CLEARDISPLAY);
		target.i2cDelay((Grove_LCD_RGB.LCD_ADDRESS), 1);

		String[] lines = text.split("\n");
		for(String line: lines) {
			writeUTF8ToRegister(target, ((Grove_LCD_RGB.LCD_ADDRESS)), LCD_SETCGRAMADDR, line.trim());
			writeSingleByteToRegister(target, ((Grove_LCD_RGB.LCD_ADDRESS)), LCD_SETDDRAMADDR, 0xc0);
		}

	}

	private static void writeSingleByteToRegister(CommandChannel target, int address, int register, int value) {
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.write(new byte[]{(byte)register,(byte)value});
			// i2cPayloadWriter.writeByte(register);
			// i2cPayloadWriter.writeByte(value);            

			target.i2cCommandClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeUTF8ToRegister(CommandChannel target, int address, int register, CharSequence text) {
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.writeByte(register);
			DataOutputBlobWriter.encodeAsUTF8(i2cPayloadWriter, text);

			target.i2cCommandClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		return 0;
	}
	@Override
	public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
		byte[] LCD_READCMD = {};
		byte[] LCD_SETUP = {};
		byte LCD_ADDR = 0x04;
		byte LCD_BYTESTOREAD = 0;
		byte LCD_REGISTER = 0;
		return new I2CConnection(this, LCD_ADDR, LCD_READCMD, LCD_BYTESTOREAD, LCD_REGISTER, LCD_SETUP);
	}
	@Override
	public boolean isGrove() {
		return false;
	}
	@Override
	public int response() {       
		return 20;      
	}
	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return true;
	}

	@Override
	public int pinsUsed() {
		return 1;
	}

}
