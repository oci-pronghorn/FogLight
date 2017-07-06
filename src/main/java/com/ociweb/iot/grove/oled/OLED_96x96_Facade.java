package com.ociweb.iot.grove.oled;

import static com.ociweb.iot.grove.oled.OLED_96x96_DriverChip.*;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.iot.maker.IODeviceFacade;




public class OLED_96x96_Facade extends OLED_DataAndCommandsSender implements IODeviceFacade{



	private int lowPixelLevel = 15 & 0x0F;
	private int highPixelLevel = (15 << 4) * 0xF0;
	private int iteration = 0;
	private OLED_96x96_DriverChip chip;

	public OLED_96x96_Facade(FogCommandChannel ch){
		//A nibble determines pixel. A byte is therefore two horizontally adjascent pixels.
		//96x96 divided 2. Since each pixel takes a nibble to send
		super(ch, new int[4608], new int[32], SSD1327_Consts.ADDRESS);
		this.chip = SSD1327;
	}
	@Deprecated
	public void setIteration(int iteration){
		this.iteration = iteration;
	}
	
	
	//TODO: FIGURE OUT WHAT CHIP
	private OLED_96x96_DriverChip determineChip(){
		if(iteration % 2 ==0){
			chip = SH1107G;
		}
		else {
			chip = SSD1327;
		}
		System.out.println(chip);
		return SH1107G;
	}
	public void setChip(int i){
		if (i == 0){
			chip = SH1107G;
		}
		else {
			chip = SSD1327;
		}
	}
	public boolean setRowColInHorizontalMode(int row, int col){
	
		switch (chip){
		case SSD1327:
			cmd_out[0] = SSD1327_Consts.REMAP;
			cmd_out[1] = SSD1327_Consts.HORIZONTAL;
			cmd_out[2] = SSD1327_Consts.SET_ROW_ADDRESS;
			cmd_out[3] = row;
			cmd_out[4] = SSD1327_Consts.SET_COL_ADDRESS;
			cmd_out[5] = col + 8; //the 8th column on the chip corresponds to the 0th column on the actual screen
			cmd_out[6] = col + 47 + 8;//end at col + 47th column, again offset by 8.
			return sendCommands(0,7);

		default:
		case SH1107G:
			return false;
		}



	}

	@Override
	public boolean setContrast(int contrast){
		cmd_out[0] = SSD1327_Consts.SET_CONTRAST_LEVEL_CMD;
		cmd_out[1] = contrast & 0xFF;
		return sendCommands(0,2);
	}

	@Override
	public boolean setTextRowCol(int row, int col){
		switch (chip){
		case SSD1327:
			cmd_out[0] = SSD1327_Consts.SET_COL_ADDRESS;
			cmd_out[1] = 8 + (col*4);
			cmd_out[2] = SSD1327_Consts.SET_ROW_ADDRESS;
			cmd_out[3] = row * 8;
			cmd_out[4] = 7 + (row * 8);	
			return sendCommands(0,5);

		case SH1107G:
			int lowCol = (col % 2  ==0) ? 0x08: 0x00;
			cmd_out[0] = (SH1107G_Consts.SET_ROW_BASE_BYTE+row);
			cmd_out[1] = (lowCol);
			cmd_out[2] = (0x11+(col/2));
			return sendCommands(0,3);

		default:
			return false;	
		}
	}

	@Override
	public boolean clear(){
		int index = 0;
		switch (chip){
		case SSD1327:

			for (int row = 0; row < OLED_96x96_Consts.ROW_COUNT >> 1; row ++){
				for (int col = 0; col < OLED_96x96_Consts.COL_COUNT; col++){
					data_out[index++] = 0x00;
				}
			}	
			return sendData(); //we send the entire array of data of 0s

		case SH1107G:

			for(int i=0; i<16;i++){

				cmd_out[0] = SH1107G_Consts.SET_ROW_BASE_BYTE + i;
				cmd_out[1] = 0x00;
				cmd_out[2] = 0x10;
				sendCommands(0,3);
				for(int j=0; j<128;j++){ 
					data_out[index++] = 0x00; 
				}
			}
			return sendData(0,2048); //2048 = 16 * 128

		default:
			return false;

		}

	}

	@Override
	public boolean init() {
		//determineChip();
		System.out.println("Artificially chose: " + chip);
		int length = generateInitCommands(); //could have done this in the return line but this is clearer.
		return sendCommands(0,length);
	}

	private int generateInitCommands(){
		switch (chip) {
		case SSD1327:
			cmd_out[0] =SSD1327_Consts.MCU;
			cmd_out[1] =SSD1327_Consts.UNLOCK_CMD_ENTERING;
			cmd_out[2] = SSD1327_Consts.DISPLAY_OFF;
			cmd_out[3] = SSD1327_Consts.SET_MULTIPLEX_RATIO;
			cmd_out[4] = 96;
			cmd_out[5] = SSD1327_Consts.SET_DISPLAY_START_LINE;
			cmd_out[6] = 0x00;
			cmd_out[7] = SSD1327_Consts.SET_DISPLAY_OFFSET;
			cmd_out[8] = 0x60;
			cmd_out[9] = SSD1327_Consts.REMAP;
			cmd_out[10] = SSD1327_Consts.VERTICAL;
			cmd_out[11] = SSD1327_Consts.SET_VDD_INTERNAL;
			cmd_out[12] = 0x01;
			cmd_out[13] = SSD1327_Consts.SET_CONTRAST_LEVEL_CMD;
			cmd_out[14] = 0x53;
			cmd_out[15] = SSD1327_Consts.SET_PHASE_LENGTH;
			cmd_out[16] = 0x51;
			cmd_out[17] = SSD1327_Consts.SET_CLOCK_DIV_RATIO;
			cmd_out[18] = 0x01;
			cmd_out[19] = 0xB9;
			cmd_out[20] = SSD1327_Consts.SET_PRECHARGE_VOLTAGE_AND_VCOMH;
			cmd_out[21] = 0x08;
			cmd_out[22] = SSD1327_Consts.SET_VCOMH;
			cmd_out[23] = 0x07;
			cmd_out[24] = SSD1327_Consts.SET_SECOND_PRECHARGE_PERIOD;
			cmd_out[25] = 0x01;
			cmd_out[26] = SSD1327_Consts.ENABLE_SECOND_PRECHARGE_AND_INTERNAL_VSL;
			cmd_out[27] = 0x62;
			cmd_out[28] = SSD1327_Consts.NORMAL_DISPLAY;
			cmd_out[29] = SSD1327_Consts.DEACTIVATE_SCROLL_CMD;
			cmd_out[30] = SSD1327_Consts.DISPLAY_ON;
			return 31;


		case SH1107G:
			cmd_out[0] = SH1107G_Consts.DISPLAY_OFF;
			cmd_out[1] = SH1107G_Consts.SET_D_CLOCK;
			cmd_out[2] = 0x50; //100Hz
			cmd_out[3] = SH1107G_Consts.SET_ROW;
			cmd_out[4] = SH1107G_Consts.SET_CONTRAST;
			cmd_out[5] = 0x80;
			cmd_out[6] = SH1107G_Consts.REMAP_SGMT;
			cmd_out[7] = SH1107G_Consts.ENTIRE_DISPLAY_ON;
			cmd_out[8] = SH1107G_Consts.NORMAL_DISPLAY;
			cmd_out[9] = SH1107G_Consts.SET_EXT_VCC;
			cmd_out[10] = 0x80;
			cmd_out[11] = SH1107G_Consts.SET_COMMON_SCAN_DIR;
			cmd_out[12] = SH1107G_Consts.SET_PHASE_LENGTH;
			cmd_out[13] = 0x1F;
			cmd_out[14] = SH1107G_Consts.SET_VCOMH_VOLTAGE;
			cmd_out[15] = 0x27;
			cmd_out[16] = SH1107G_Consts.DISPLAY_ON;
			cmd_out[17] = SH1107G_Consts.SET_ROW_BASE_BYTE;
			cmd_out[18] = 0x00;
			cmd_out[19] = 0x11;

			return 20;
		default:
			return 0;

		}

	}

	@Override
	public boolean cleanClear() {
		return displayOff() && clear() && displayOn();
	}

	@Override
	public boolean displayOn() {
		return sendCommand(SSD1327_Consts.DISPLAY_ON);
	}

	@Override
	public boolean displayOff() {
		return sendCommand(SSD1327_Consts.DISPLAY_OFF);
	}

	@Override
	public boolean printCharSequence(CharSequence s) {
		encodeCharSequence(s);
		//TODO: decide if display needs to be in vertical mode
		if (chip == SSD1327 && !setVerticalMode()){
			//setVerticalMode would only execute if the chip is SSD1327
			return false;
		}
		int charSpace = 0;
		switch(chip){
		case SSD1327:
			charSpace = 32;
			break;
		case SH1107G:
			charSpace = 8;
			break;
		}
		return sendData(0, s.length()* charSpace);
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
		int charSpace = 0;
		
		switch(chip){
		case SSD1327:
			charSpace = 32;
			break;
		case SH1107G:
			charSpace = 8;
			break;
		}
		for (int i = startingIndex; i < startingIndex + s.length(); i++){
			encodeChar(s.charAt(charIndex++), (i * charSpace));
			//each char printed takes up 32 bytes of transmission  (8x8 chars) where each pixel take up a nibble.
		}
	}
	private void encodeChar(char c, int startingIndex){
		if(c < 32 || c > 127){
			c=' '; 
		}
		
		switch(chip){
		case SSD1327:
			for (int i =0; i < 8; i += 2){
				for(char j=0;j<8;j++)
				{
					char newC = 0x00;
					//"Character is constructed two pixel at a time using vertical mode from the default 8x8 font"-Seeed C++ l API
					newC |= (highBitAt(OLED_96x96_Consts.FONT[c-32][i],j))? highPixelLevel:0x00;
					newC |= (highBitAt(OLED_96x96_Consts.FONT[c-32][i+1],j))? lowPixelLevel:0x00;
					
					
					data_out[startingIndex++] = newC;
				}
			}
			return;
		case SH1107G:
			for (int i = 0; i < 8; i ++){
				data_out[startingIndex++] = OLED_96x96_Consts.FONT[c-32][i];
			}

		default:
			return;
		}


	}

	private boolean sendCommandsInQuickSuccession(int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		assert(length < OLED_96x96_Consts.BATCH_SIZE);
		assert(start + length < OLED_96x96_Consts.BATCH_SIZE);
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		i2cPayloadWriter.write(SSD1327_Consts.MCU);
		i2cPayloadWriter.write(SSD1327_Consts.UNLOCK_CMD_ENTERING);
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
		switch(chip){
		case SSD1327:
			int index = 0;
			if ( !setHorizontalMode()){
				return false;
			}

			for (int i = 0; i < bitmap.length; i++){
				for (int j = 0; j < 8; j = j+2){
					int c = 0x00;
					int b1 = (bitmap[i] << j) & 0x80;
					int b2 = (bitmap[i] << (j+1)) & 0x80;

					c |= (b1 > 0)? highPixelLevel:0x00;
					c |= (b2 > 0)? lowPixelLevel:0x00;		
					data_out[index++] = c;	
				}
			}
			sendData(0, bitmap.length*4);
		case SH1107G:
			int row = 0;
			int lowCol = 0x00;
			int highCol = 0x11;
			if (!setHorizontalMode()){
				return false;
			}
			for (int i = 0; i < bitmap.length; i++){
				cmd_out[0] = (SH1107G_Consts.SET_ROW_BASE_BYTE + row);
				cmd_out[1] = lowCol;
				cmd_out[2] = highCol;	
				sendCommands(0,3);
				int curByte = bitmap[i];
				int tmp = 0x00;
				for (int j = 0; j < 8; j++){
					tmp |= ((curByte >> (7 - j)) & 0x01) << j;
				}
				
				data_out[i] = tmp;
				row++;
				lowCol ++;
				if (lowCol >= 16){
					lowCol= 0x00;
					highCol += 0x01;
				}
			}
			return sendData(0, bitmap.length);
			
		default:
			return false;

		}

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
		return sendCommand(SSD1327_Consts.INVERSE_DISPLAY); //the two chips share the same command byte
	}

	@Override
	public boolean inverseOff() {
		return sendCommand(SSD1327_Consts.NORMAL_DISPLAY); //the two chips share the same command byte
	}

	@Override
	public boolean displayImage(int[][] raw_image) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setHorizontalMode() {
		switch(chip){
		case SSD1327:
			return setRowColInHorizontalMode(0,0);

		case SH1107G:
			cmd_out[0] = SH1107G_Consts.REMAP_SGMT;
			cmd_out[1] = SH1107G_Consts.SET_HORIZONTAL;
			return sendCommands(0,2);

		default:
			return false;
		}

	}

	@Override
	public boolean setVerticalMode() {
		switch(chip){
		case SSD1327:
			data_out[0] = SSD1327_Consts.REMAP;
			data_out[1] = SSD1327_Consts.VERTICAL;
			break;
		case SH1107G:
			data_out[0] = SH1107G_Consts.REMAP_SGMT;
			data_out[1] = SH1107G_Consts.SET_VERTICAL;
			break;
		default:
			return false;
		}

		return sendCommands(0,2);
	}
}
