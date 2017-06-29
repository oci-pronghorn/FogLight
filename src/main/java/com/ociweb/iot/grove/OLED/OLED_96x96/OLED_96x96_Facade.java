package com.ociweb.iot.grove.OLED.OLED_96x96;

import static com.ociweb.iot.grove.OLED.OLED_96x96.Grove_OLED_96x96_Constants.*;

import com.ociweb.iot.grove.OLED.OLED_DataAndCommandsSender;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.iot.maker.IODeviceFacade;
import static com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_DriverChip.*;




public class OLED_96x96_Facade extends OLED_DataAndCommandsSender implements IODeviceFacade{



	private int lowPixelLevel = 0;
	private int highPixelLevel = 15;

	private OLED_96x96_DriverChip chip;

	public OLED_96x96_Facade(FogCommandChannel ch){
		//A nibble determines pixel. A byte is therefore two horizontally adjascent pixels.
		//96x96 divided 2. Since each pixel takes a nibble to send
		super(ch, new int[4608], new int[32], ADDRESS);
		this.chip = SH1107G;
	}
	
	//TODO: FIGURE OUT WHAT CHIP
	private OLED_96x96_DriverChip determineChip(){
		return SSD1327;
	}
	public boolean setRowColInHorizontalMode(int row, int col){
		switch (chip){
		case SSD1327:
			cmd_out[0] = REMAP;
			cmd_out[1] = HORIZONTAL;
			cmd_out[2] = SET_ROW_ADDRESS;
			cmd_out[3] = row;
			cmd_out[4] = SET_COL_ADDRESS;
			cmd_out[5] = col + 8; //the 8th column on the chip corresponds to the 0th column on the actual screen
			cmd_out[6] = col + 47 + 8;//end at col + 47th column, again offset by 8.
			break;
		case SH1107G:
			break;
		}
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
		generateInitCommands();
		return sendCommands(0,31);
	}

	private void generateInitCommands(){
		cmd_out[0] =MCU;
		cmd_out[1] =UNLOCK_CMD_ENTERING;
		cmd_out[2] = DISPLAY_OFF;
		cmd_out[3] = SET_MULTIPLEX_RATIO;
		cmd_out[4] = 96;
		cmd_out[5] = SET_DISPLAY_START_LINE;
		cmd_out[6] = 0x00;
		cmd_out[7] = SET_DISPLAY_OFFSET;
		cmd_out[8] = 0x60;
		cmd_out[9] = REMAP;
		cmd_out[10] = VERTICAL;
		cmd_out[11] = SET_VDD_INTERNAL;
		cmd_out[12] = 0x01;
		cmd_out[13] = SET_CONTRAST_LEVEL_CMD;
		cmd_out[14] = 0x53;
		cmd_out[15] = SET_PHASE_LENGTH;
		cmd_out[16] = 0x51;
		cmd_out[17] = SET_CLOCK_DIV_RATIO;
		cmd_out[18] = 0x01;
		cmd_out[19] = 0xB9;
		cmd_out[20] = SET_PRECHARGE_VOLTAGE_AND_VCOMH;
		cmd_out[21] = 0x08;
		cmd_out[22] = SET_VCOMH;
		cmd_out[23] = 0x07;
		cmd_out[24] = SET_SECOND_PRECHARGE_PERIOD;
		cmd_out[25] = 0x01;
		cmd_out[26] = ENABLE_SECOND_PRECHARGE_AND_INTERNAL_VSL;
		cmd_out[27] = 0x62;
		cmd_out[28] = NORMAL_DISPLAY;
		cmd_out[29] = DEACTIVATE_SCROLL_CMD;
		cmd_out[30] = DISPLAY_ON;
	}

	@Override
	public boolean cleanClear() {
		return displayOff() && clear() && displayOn();
	}

	@Override
	public boolean displayOn() {
		return sendCommand(DISPLAY_ON);
	}

	@Override
	public boolean displayOff() {
		return sendCommand(DISPLAY_OFF);
	}

	@Override
	public boolean printCharSequence(CharSequence s) {
		encodeCharSequence(s);
		//TODO: decide if display needs to be in vertical mode
		return setVerticalMode() && sendData(0, s.length()* 32);
	}

	@Override
	public boolean printCharSequenceAt(CharSequence s, int row, int col) {
		return setTextRowCol(0,0) && printCharSequence(s);
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

	private boolean sendCommandsInQuickSuccession(int start, int length){
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
