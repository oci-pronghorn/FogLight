package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

import static com.ociweb.iot.grove.Grove_OLED_128x64_Constants.*;
public class Grove_OLED_128x64 implements IODevice{

	public static boolean isStarted = false;
	public static boolean init(FogCommandChannel target){
		if (!target.i2cIsReady()) {
			return false;
		}
		isStarted = true;


		sendCommand(target, PUT_DISPLAY_TO_SLEEP);     //display off
		sendCommand(target, WAKE_DISPLAY);  //display on
		sendCommand(target, TURN_OFF_INVERSE_DISPLAY);  //Set Normal Display (default)

		target.i2cFlushBatch();

		return isStarted;
	}




	public static boolean sendCommand(FogCommandChannel ch, int b){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		i2cPayloadWriter.write(Grove_OLED_128x64_Constants.COMMAND_MODE);
		i2cPayloadWriter.write(b);
		ch.i2cCommandClose();

		return true;
	}

	public static boolean sendData(FogCommandChannel ch, int b ){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		i2cPayloadWriter.write(Grove_OLED_128x64_Constants.DATA_MODE);
		i2cPayloadWriter.write(b);
		ch.i2cCommandClose();
		return true;
	}

	public static boolean setContrast(FogCommandChannel ch, int contrast){
		if (sendCommand(ch, SET_CONTRAST_CONTROL) && sendCommand(ch, (byte)(contrast & 0xFF))){
			ch.i2cFlushBatch();
			return true;
		}
		else {
			ch.i2cFlushBatch();
			return false;
		}

	}

	public static boolean setPageMode(FogCommandChannel ch){
		if (sendCommand(ch,SET_MEMORY) && sendCommand(ch, (byte) 0x02) ){
			ch.i2cFlushBatch();
			return true ;
		}
		else {
			ch.i2cFlushBatch();
			return false;
		}
	}

	public static boolean setHorizontalMode(FogCommandChannel ch){
		if (sendCommand(ch,SET_MEMORY) && sendCommand(ch, (byte)0x00)){
			ch.i2cFlushBatch();
			return true;
		}
		else {
			ch.i2cFlushBatch();
			return false;
		}
	}

	public static boolean setVerticalMode(FogCommandChannel ch){
		if (sendCommand(ch,SET_MEMORY) && sendCommand(ch, (byte)0x01)){
			ch.i2cFlushBatch();
			return true;
		}
		else {
			ch.i2cFlushBatch();
			return false;
		}
	}

	/**
	 * NOTE: this method leaves the display in horizontal mode
	 * @param ch
	 * @param map
	 * @return true if the i2c commands were succesfully sent, false otherwise
	 */
	public static boolean drawBitmap(FogCommandChannel ch, int[] map){
		if (!setHorizontalMode(ch)){

			return false;
		}
		for (int bitmap: map){

			if (!sendData(ch, (byte) bitmap)){
				return false;
			}
			ch.i2cFlushBatch();
		}

		return true;
	}

	public static boolean turnOnInverseDisplay(FogCommandChannel ch){
		if(sendCommand(ch, TURN_ON_INVERSE_DISPLAY)){
			ch.i2cFlushBatch();
			return true;

		}
		ch.i2cFlushBatch();
		return false;
	}

	public static boolean turnOffInverseDisplay(FogCommandChannel ch){
		if(sendCommand(ch, TURN_OFF_INVERSE_DISPLAY)){
			ch.i2cFlushBatch();
			return true;
		}
		ch.i2cFlushBatch();
		return false;
	}

	public static boolean putChar(FogCommandChannel ch, char c){
		if (c > 127 || c < 32){
			//'c' has no defined font for Grove_OLED_128x64");
			return false;
		}
		for (int i = 0; i < 8; i++){
			if (sendData(ch, (byte)BASIC_FONT[c][i])){	//successful send is expected to be more common
				ch.i2cFlushBatch();
			}
			else {
				ch.i2cFlushBatch();
				return false;
			}
		}
		return true;
	}
	public static boolean setTextXY(FogCommandChannel ch, int x, int y){
		//bit-mask because x and y can only be within a certain range (0-7)
		
		//TODO: avoid three seperate if-statements by ANDing them in the condtional, is there a better way?
		if (sendCommand(ch, (ROW_START_ADDRESS_PAGE_MODE + (x & 0x07))) 
				&& sendCommand(ch,  (LOWER_COL_START_ADDRESS_PAGE_MODE + (8*y & 0x0F)))
				&& sendCommand(ch,  (HIGHER_COL_START_ADDRESS_PAGE_MODE) + (8*y >> 4 & 0x0F)))
		{
			ch.i2cFlushBatch();
			return true;
		}
		ch.i2cFlushBatch();
		return false;
	}
	
	public static boolean setPageModeAndTextXY(FogCommandChannel ch, int x, int y){
		return setPageMode(ch) && setTextXY(ch,x,y);
		
	}

	private static boolean writeByteSequence(FogCommandChannel ch, byte[] seq){
		if(!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		i2cPayloadWriter.write(seq);
		ch.i2cCommandClose();
		return true;
	}

	//Overloading the function to automatically mask ints and use their least significant 8-bits as our bytes to send
	//Ideally, for best performance, we should send byte array and not int array to avoid this extra function call
	private static boolean writeByteSequence(FogCommandChannel ch, int[] seq){
		byte[] byteSeq = new byte[seq.length];
		int counter = 0;
		for (int i: seq){
			byteSeq[counter] = (byte)(i & 0xFF); //this mask turns anything but the smallest 8 bits to 0
			counter++;
		}
		return writeByteSequence(ch, byteSeq);
	}

	@Override
	public int response() {
		return 20;
	}

	@Override
	public int scanDelay() {
		return 0;
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
	public I2CConnection getI2CConnection() {
		byte[] LED_READCMD = {};
		byte[] LED_SETUP = {};
		byte LED_ADDR = 0x04;
		byte LED_BYTESTOREAD = 0;
		byte LED_REGISTER = 0;
		return new I2CConnection(this, LED_ADDR, LED_READCMD, LED_BYTESTOREAD, LED_REGISTER, LED_SETUP);
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
