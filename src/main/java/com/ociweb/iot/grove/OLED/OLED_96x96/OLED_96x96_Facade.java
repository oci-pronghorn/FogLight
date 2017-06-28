package com.ociweb.iot.grove.OLED.OLED_96x96;

import static com.ociweb.iot.grove.OLED.OLED_96x96.Grove_OLED_96x96_Constants.*;

import com.ociweb.iot.grove.OLED.OLED_DataAndCommandsSender;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.iot.maker.Facade;


public class OLED_96x96_Facade extends OLED_DataAndCommandsSender implements Facade{

	private int lowPixelLevel = 0;
	private int highPixelLevel = 15;
	
	public OLED_96x96_Facade(FogCommandChannel ch){
		//A nibble determines pixel. A byte is therefore two horizontally adjascent pixels.
		//96x96 divided 2. Since each pixel takes a nibble to send
		super(ch, new int[4608], new int[32], ADDRESS);
	}

	public boolean setRowColInHorizontalMode(int row, int col){
		cmd_out[0] = REMAP;
		cmd_out[1] = HORIZONTAL;
		cmd_out[2] = SET_ROW_ADDRESS;
		cmd_out[3] = row;
		cmd_out[4] = SET_COL_ADDRESS;
		cmd_out[5] = col + 8; //the 8th column on the chip corresponds to the 0th column on the actual screen
		cmd_out[6] = col + 47 + 8;//end at col + 47th column, again offset by 8.
		return sendCommands(0,7);
	}

	@Override
	public boolean setContrast(int contrast){
		cmd_out[0] = SET_CONTRAST_LEVEL_CMD;
		cmd_out[1] = contrast & 0xFF;
		return sendCommands(0,2);
	}

	@Override
	public boolean setTextRowCol(int row, int col){
		cmd_out[0] = SET_COL_ADDRESS;
		cmd_out[1] = 8 + (col*4);
		cmd_out[2] = SET_ROW_ADDRESS;
		cmd_out[3] = row * 8;
		cmd_out[4] = 7 + (row * 8);	
		return sendCommands(0,5);
	}

	@Override
	public boolean clear(){
		int index = 0;
		for (int row = 0; row < ROW_COUNT >> 1; row ++){
			for (int col = 0; col < COL_COUNT; col++){
				data_out[index++] = 0x00;
			}
		}	
		return sendData(); //we send the entire array of data of 0s
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cleanClear() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean displayOn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean displayOff() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printCharSequence(CharSequence s) {
		encodeCharSequence(s);
		//TODO: decide if display needs to be in vertical mode
		return setVerticalMode() && sendData(0, s.length()* 32);
	}
	
	@Override
	public boolean printCharSequenceAt(CharSequence s, int row, int col) {
		return false;
	}

	
	/**
	 * Encodes charSequence as the proper bytes to be send into the data_out array. With no index supplied, the method
	 * starts filling up the bytes at index 0 of the data_out array.
	 * Impl: calls {@link #encodeCharSequence(CharSequence, int)} with index defaulted to 0.
	 * @param s
	 */
	private void encodeCharSequence(CharSequence s){
		encodeCharSequence(s,0);
	}
	private void encodeCharSequence(CharSequence s, int startingIndex){
		int charIndex = 0;
		for (int i = startingIndex; i < startingIndex + s.length(); i++){
			encodeChar(s.charAt(charIndex++), startingIndex * 32);
			//each char printed takes up 32 bytes of transmission  (8x8 chars) where each pixel take up a nibble.
		}
	}
	private void encodeChar(char c, int startingIndex){
		if(c < 32 || c > 127){
			c=' '; 
		}
		for (int i =0; i < 8; i += 2){
			for(char j=0;j<8;j++)
			{
				//"Character is constructed two pixel at a time using vertical mode from the default 8x8 font"-Seeed C++ l API
				c |= (highBitAt(FONT[c-32][i],j))? highPixelLevel:lowPixelLevel;
				c |= (highBitAt(FONT[c-32][i+1],j))? highPixelLevel:lowPixelLevel;
				data_out[startingIndex++] = c;
			}
		}

	}
	
	private boolean sendCommanndsInQuickSuccession(int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		assert(length < BATCH_SIZE);
		assert(start + length < BATCH_SIZE);
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		i2cPayloadWriter.write(MCU);
		i2cPayloadWriter.write(UNLOCK_CMD_ENTERING);
		for (int i = start; i < start + length; i ++){
			
			i2cPayloadWriter.write(cmd_out[i]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		
		return true;
	}
	
	private boolean highBitAt(int b, int pos){
		return ((b >> pos) & 0x01) == 0x01;
	}


	@Override
	public boolean drawBitmap(int[] bitmap) {
		if ( !setHorizontalMode()){
			return false;
		}

		return false;
	}

	@Override
	public boolean activateScroll() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deactivateScroll() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setUpScroll() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inverseOn() {
		return sendCommand(INVERSE_DISPLAY);
	}

	@Override
	public boolean inverseOff() {
		return sendCommand(NORMAL_DISPLAY);
	}

	@Override
	public boolean displayImage(int[][] raw_image) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setHorizontalMode() {
		return setRowColInHorizontalMode(0,0);
	}

	@Override
	public boolean setVerticalMode() {
		data_out[0] = REMAP;
		data_out[1] = VERTICAL;
		return sendCommands(0,2);
	}
}
