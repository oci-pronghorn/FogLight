package com.ociweb.iot.grove;

import java.io.IOException;
import java.util.Arrays;

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
 * @author Alex Herriott
 */
public class Grove_LCD_RGB implements IODevice{ 


	public static boolean isStarted = false;

	// Current LCD_DISPLAYCONTROL states
	private static int LCD_DISPLAY =Grove_LCD_RGB_Constants.LCD_DISPLAYON;
	private static int LCD_CURSOR =Grove_LCD_RGB_Constants.LCD_CURSOROFF;
	private static int LCD_BLINK =Grove_LCD_RGB_Constants.LCD_BLINKOFF;


	public static boolean begin(CommandChannel target){
		if (!target.i2cIsReady()) {
			return false;
		}
		isStarted = true;

		writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_TWO_LINES);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 5*Grove_LCD_RGB_Constants.MS_TO_NS);  // wait more than 4.1ms

		// second try
		writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_TWO_LINES);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1*Grove_LCD_RGB_Constants.MS_TO_NS);

		// third go
		writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_TWO_LINES);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1*Grove_LCD_RGB_Constants.MS_TO_NS);


		// turn the display on with no cursor or blinking default
		setDisplayControl(target);

		// clear it off
		displayClear(target);

		// set the entry mode
		//writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, LCD_SETDDRAMADDR, LCD_ENTRYMODESET | LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1*Grove_LCD_RGB_Constants.MS_TO_NS);

		setCursor(target, 0, 0);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1*Grove_LCD_RGB_Constants.MS_TO_NS);
		target.i2cFlushBatch();
		return true;
	}

	/**
	 * <pre>
	 * Creates a complete byte array that will set the text and color of a Grove RGB
	 * LCD display when passed to a {@link com.ociweb.pronghorn.stage.test.ByteArrayProducerStage}
	 * which is using chunk sizes of 3 and is being piped to a {@link I2CStage}.
	 *
	 * TODO: This function is currently causing the last letter of the text to be dropped
	 *       when displayed on the Grove RGB LCD; there's currently a work-around that
	 *       simply appends a space to the incoming text variable, but it's hackish
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

	/**
	 * Switches the lcd display on or off. Default is on
	 * 
	 * @param target CommandChannel to send command on
	 * @param on boolean to set display to
	 * @return boolean command is successful
	 */
	public static boolean commandForDisplay (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_DISPLAY = Grove_LCD_RGB_Constants.LCD_DISPLAYON;
		}else{
			LCD_DISPLAY = Grove_LCD_RGB_Constants.LCD_DISPLAYOFF;
		}
		setDisplayControl(target);  
		target.i2cFlushBatch();
		return true;
	}

	/**
	 * Switches the cursor on or off. Default is off
	 * 
	 * @param target CommandChannel to send command on
	 * @param on boolean to set cursor to
	 * @return boolean command is successful
	 */
	public static boolean commandForCursor (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_CURSOR = Grove_LCD_RGB_Constants.LCD_CURSORON;
		}else{
			LCD_CURSOR = Grove_LCD_RGB_Constants.LCD_CURSOROFF;
		}

		setDisplayControl(target); 
		target.i2cFlushBatch();

		return true;
	}

	/**
	 * Switches the blinking cursor on or off. Default is off
	 * 
	 * @param target CommandChannel to send command on
	 * @param on boolean to set blink to
	 * @return boolean command is successful
	 */
	public static boolean commandForBlink (CommandChannel target, boolean on){
		if (!target.i2cIsReady()) {
			return false;
		}

		if(on){
			LCD_BLINK = Grove_LCD_RGB_Constants.LCD_BLINKON;
		}else{
			LCD_BLINK = Grove_LCD_RGB_Constants.LCD_BLINKOFF;
		}

		setDisplayControl(target);        
		target.i2cFlushBatch();

		return true;
	}
	
	public static boolean clearDisplay(CommandChannel target){
		if (!target.i2cIsReady()) {
			return false;
		}
		displayClear(target);

		target.i2cFlushBatch();
		return true;
	}

	public static boolean setCursor(CommandChannel target, int col, int row){
		if (!target.i2cIsReady()) {
			return false;
		}
		col = (row == 0 ? col|0x80 : col|0xc0);
		writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, col);
		target.i2cFlushBatch();
		return true;
	}
	
	
	
	/////////////////////////////
	////  Write text methods ////
	/////////////////////////////

	/**
	 * Creates a custom char from an array of 8 bytes. Can save up to 8 custom chars in the LCD. 
	 * @param target CommandChannel to send command on
	 * @param location location 0-7 to store the charmap in the LCD
	 * @param charMap Array of 8 bytes. Each byte is a row. Least significant 5 bits determines values within row
	 */
	public static boolean setCustomChar(CommandChannel target, int location,  byte charMap[]){
		if (!target.i2cIsReady()) {
			return false;
		}
		if(!isStarted){
			begin(target);
		}
		assert(location < 8 && location >= 0) : "Only locations 0-7 are valid";
		assert(charMap.length == 8) : "charMap must contain an array of 8 bytes";
		location &= 0x7;
		for (int i = 0; i < charMap.length; i++) {
			charMap[i] &= 0x1F; //each element contains 5 bits
		}

		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)), Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR | (location<<3));
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.CGRAM_SET_DELAY);
		writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, charMap, 0, charMap.length);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.DDRAM_WRITE_DELAY);
		target.i2cFlushBatch();

		//begin(target); //TODO: Seems to be necessary, but shouldn't be
		return true;
	}

	/**
	 * Writes an ascii char  with idx characterIdx. Locations 0-7 contain custom characters.
	 * @param target CommandChannel to send command on
	 * @param characterIdx Index of the character
	 * @param row TODO
	 * @param col TODO
	 */
	public static boolean writeChar(CommandChannel target, int characterIdx, int col, int row){
		if (!target.i2cIsReady()) {
			return false;
		}
		setCursor(target, col, row);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)), Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, characterIdx);
		target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.DDRAM_WRITE_DELAY);
		target.i2cFlushBatch();

		return true;
	}

	public static boolean writeMultipleChars(CommandChannel target, byte[] characterIdx, int col, int row){ //TODO: creates lots of garbage
		return writeMultipleChars(target, characterIdx, 0, characterIdx.length, col, row);
	}
	
	public static boolean writeMultipleChars(CommandChannel target, byte[] characterIdx, int startIdx, int length, int col, int row){
		if (!target.i2cIsReady()) {
			return false;
		}
		int iterator = startIdx;
		int endOfLineIdx = 16-col;
		int steps = 4;
		setCursor(target, col, row);

		while(iterator<length){
			if(endOfLineIdx<Math.min(iterator+steps,length-1)){
				writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, 
						characterIdx, iterator, endOfLineIdx-iterator);
				iterator = endOfLineIdx;
				endOfLineIdx+=16;
				row = (row+1)&1;
				setCursor(target, col, row);
			}else{
				writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, 
						characterIdx, iterator, Math.min(steps, length-iterator));
				iterator += steps;		
			}
		}
		target.i2cFlushBatch();
		return true;
	}
	
	public static boolean writeCharSequence(CommandChannel target, CharSequence text, int col, int row){
		return writeCharSequence(target, text, 0, text.length(), col, row);
	}
	
	public static boolean writeCharSequence(CommandChannel target, CharSequence text, int startIdx, int length, int col, int row){
		if (!target.i2cIsReady()) {
			return false;
		}
		int iterator = startIdx;
		int endOfLineIdx = 16-col;
		int steps = 4;
		setCursor(target, col, row);
		
		while(iterator<length){
			if(endOfLineIdx<Math.min(iterator+steps,length-1)){
				writeCharSequenceToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, 
						text, iterator, endOfLineIdx-iterator);
				iterator = endOfLineIdx;
				endOfLineIdx+=16;
				row = (row+1)&1;
				setCursor(target, col, row);
			}else{
				writeCharSequenceToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, 
						text, iterator, Math.min(steps, length-iterator));
				iterator += steps;		
			}
		}
		
		target.i2cFlushBatch();
		return true;
	}
	
	public static boolean writePaddedInt(CommandChannel target, int value, int length, int col, int row){
		if (!target.i2cIsReady()) {
			return false;
		}
		writeCharSequence(target, String.format("%0"+length+"d", value), col, row);
		return true;
	}



	





	//////////////////////////////
	///    Private Methods    ////
	//////////////////////////////

	private static void setDisplayControl(CommandChannel target){
		writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, 
				Grove_LCD_RGB_Constants.LCD_DISPLAYCONTROL | LCD_DISPLAY | LCD_CURSOR | LCD_BLINK);
		target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.DISPLAY_SWITCH_DELAY);
	}



	private static void showRGBColor(CommandChannel target, int r, int g, int b) {
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 0, 0);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 1, 0);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 0x08, 0xaa);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 4, r);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 3, g);
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 2, b);
	}

	private static void displayClear(CommandChannel target) {
		//clear display
		writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)), Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_CLEARDISPLAY);
		target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.SCREEN_CLEAR_DELAY);

	}

	private static void showTwoLineText(CommandChannel target, String text) {

		if(!isStarted){
			begin(target);
		}
		displayClear(target);
		String[] lines = text.split("\n");
		int steps = 4;
		
		target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.INPUT_SET_DELAY);
		
		for(String line: lines) {
			int p = 0;
			while (p<line.length()) {
				writeUTF8ToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)), Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, line, p, Math.min(steps, line.length()-p) );
				p+=steps;

			}
			//new line
			writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)), Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, 0xc0);
		}

	}

	private static void writeSingleByteToRegister(CommandChannel target, int address, int register, int value) {
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.writeByte(register);
			i2cPayloadWriter.writeByte(value);          

			target.i2cCommandClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeMultipleBytesToRegister(CommandChannel target, int address, int register, byte[] values, int startIdx, int length) {
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.writeByte(register);
			for (int i = startIdx; i < startIdx+length; i++) {
				i2cPayloadWriter.writeByte(values[i]);
			}

			target.i2cCommandClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeCharSequenceToRegister(CommandChannel target, int address, int register, CharSequence values, int startIdx, int length){
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.writeByte(register);
			i2cPayloadWriter.writeASCII(values.subSequence(startIdx, startIdx+length));

			target.i2cCommandClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	};
	private static void writeUTF8ToRegister(CommandChannel target, int address, int register, CharSequence text, int pos, int len) {
		try {
			DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

			i2cPayloadWriter.writeByte(register);
			DataOutputBlobWriter.encodeAsUTF8(i2cPayloadWriter, text, pos, len);

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
