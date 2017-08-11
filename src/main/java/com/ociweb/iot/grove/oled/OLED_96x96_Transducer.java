package com.ociweb.iot.grove.oled;

import static com.ociweb.iot.grove.oled.OLED_96x96_DriverChip.*;

import com.ociweb.gl.api.transducer.StartupListenerTransducer;
import com.ociweb.iot.grove.oled.OLED_96x96_Consts;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.image.FogBitmapLayout;
import com.ociweb.iot.maker.image.FogColorSpace;
import com.ociweb.iot.maker.image.FogPixelScanner;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.iot.maker.IODeviceTransducer;

public class OLED_96x96_Transducer extends BinaryOLED implements IODeviceTransducer, StartupListenerTransducer{

	private int lowPixelLevel = 0x0F;
	private int highPixelLevel = 0xF0;
	private int iteration = 0;
	private OLED_96x96_DriverChip chip;
	private boolean clearScreenUponStartup = true;

	public OLED_96x96_Transducer(FogCommandChannel ch){
		//A nibble determines pixel. A byte is therefore two horizontally adjascent pixels.
		//96x96 divided 2. Since each pixel takes a nibble to send
		super(ch, new int[4608], new int[32], SSD1327_Consts.ADDRESS);
		ch.ensureI2CWriting(100, BATCH_SIZE);
		this.chip = SSD1327;

	}

	@Override
	public FogBitmapLayout createBmpLayout() {
		FogBitmapLayout bmpLayout = new FogBitmapLayout(FogColorSpace.gray);
		bmpLayout.setComponentDepth((byte) 4);
		bmpLayout.setWidth(OLED_96x96_Consts.COL_COUNT);
		bmpLayout.setHeight(OLED_96x96_Consts.ROW_COUNT);
		return bmpLayout;
	}

	@Override
	public boolean display(FogPixelScanner scanner) {
		// Assume progressive scanner for now...
		while (scanner.next((bmp, i, x, y) -> {
			if (x % 2 == 0) {
				byte v1 = (byte)bmp.getComponent(x , y, 0);
				byte v2 = (byte)bmp.getComponent(x + 1, y, 0);
				byte packet = (byte)((v1 << 4) | v2);
			}
		}));
		return true;
	}

	@Deprecated
	public void setIteration(int iteration){
		this.iteration = iteration;
	}

	public void setTextBrightness(int brightness){
		lowPixelLevel = (brightness) & 0x0F;
		highPixelLevel = (brightness << 4) & 0xF0;
	}

	//TODO: FIGURE OUT WHAT CHIP
	private OLED_96x96_DriverChip determineChip(){
		if(iteration % 2 ==0){
			chip = SSD1327;
		}
		else {	
			chip = SH1107G;
		}
		System.out.println(chip);
		return SH1107G;
	}
	@Deprecated
	public void setChip(int i){
		if (i == 0){
			chip = SH1107G;
		}
		else {
			chip = SSD1327;
		}

	}
	/** Sets contrast level for the screen.
	 * @param contrast 
	 * @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean setContrast(int contrast){
		cmd_out[0] = SSD1327_Consts.SET_CONTRAST_LEVEL_CMD;
		cmd_out[1] = contrast & 0xFF;
		return sendCommands(0,2);
	}

	/** Sets the column aand row address of the text printing function.
	 * @param row
	 * @param col
	 * @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean setTextRowCol(int row, int col){
		switch (chip){
		case SSD1327:
			cmd_out[0] = SSD1327_Consts.SET_COL_ADDRESS;
			cmd_out[1] = 0x08 + (col*4);
			cmd_out[2] = 0x37; //end column
			cmd_out[3] = SSD1327_Consts.SET_ROW_ADDRESS;
			cmd_out[4] = 0x00 + (row * 8);
			cmd_out[5] = 0x07 + (row * 8);	
			return sendCommands(0,6);

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

	/**
	 * clears the screen by prints spaces to the entire screen.
	 *@return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean clear(){
		int index = 0;
		switch (chip){
		case SSD1327:
			setVerticalMode();
			for (int row = 0; row < OLED_96x96_Consts.ROW_COUNT / 8; row++ ){
				setTextRowCol(row,0);
				if  (!printCharSequence("            ")){ //12 spaces is one empty row
					return false;
				}
			}
			return true;

		case SH1107G:
			for(int j=0; j<128;j++){ 
				data_out[j] = 0x00;  //make an empty array to be sent repeatedly
			} 
			for(int i=0; i<16;i++){

				cmd_out[0] = SH1107G_Consts.SET_ROW_BASE_BYTE + i;
				cmd_out[1] = 0x00;
				cmd_out[2] = 0x10;
				if (! sendCommands(0,3) || !sendData(0,128)){
					return false;
				}
			}
			return true; //2048 = 16 * 128

		default:
			return false;

		}

	}

	@Override
	protected boolean init() {
		//determinChip(); TODO: this would be the place to call determineChip once implemented.
		System.out.println("Chipset chosen: " + chip);
		return sendCommands(0,  generateInitCommands() ); //generateInitCommands() returns the length of the commands generated
	}

	private int generateInitCommands(){
		switch (chip) {
		case SSD1327:
			cmd_out[0] =SSD1327_Consts.MCU;
			cmd_out[1] =SSD1327_Consts.UNLOCK_CMD_ENTERING;
			cmd_out[2] = SSD1327_Consts.DISPLAY_OFF;
			cmd_out[3] = SSD1327_Consts.SET_MULTIPLEX_RATIO;
			cmd_out[4] = 0x5F;
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

	/**
	 * Clears the screen while the screen is off and turns the screen backs on afterwards.
	 * @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean cleanClear() {
		return displayOff() && clear() && displayOn();
	}

	/**
	 * Turns screen on.
	 * @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean displayOn() {
		return sendCommand(SSD1327_Consts.DISPLAY_ON);
	}


	/**
	 * Turns screen off.
	 * @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean displayOff() {
		return sendCommand(SSD1327_Consts.DISPLAY_OFF);
	}

	/**
	 * Prints the charSequence at the current textRowCol position.
	 * @param s is the char sequence to be printed. A String can be supplied since String is a subclass of CharSequence.
	 * @return true if the channel was ready for the i2c commands.
	 */

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

	/**
	 * Prints the charSequence at the specified textRowCol position.
	 * @param s is the char sequence to be printed. A String can be supplied since String is a subclass of CharSequence.
	 * @param row
	 * @param col
	 * @return true if the channel was ready for the i2c commands.
	 */
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


	public boolean drawPixelMap(int[] map){
		return true;
	}

	/**
	 * This function should be used over {@link #display(int[][], int)} with the pixelDepth set to 1 if the
	 * input data map is a one dimensional compact array where each bit corresponds to a pixel. If each int corresponds
	 * to a pixel, {@link #display(int[][], int)} should be the go-to method.
	 * @param map
	 * @return true if the channel was ready for the i2c commands.
	 */
	public boolean drawBitmap(int[] map) {
		switch(chip){
		case SSD1327:
			int index = 0;
			if ( !setHorizontalMode()){
				return false;
			}

			for (int i = 0; i < map.length; i++){
				for (int j = 0; j < 8; j = j+2){
					int c = 0x00;
					int b1 = (map[i] << j) & 0x80;
					int b2 = (map[i] << (j+1)) & 0x80;

					c |= (b1 > 0)? highPixelLevel:0x00;
					c |= (b2 > 0)? lowPixelLevel:0x00;		

					data_out[index++] = c;	
				}
			}
			return sendData(0, map.length*4);
		case SH1107G:
			int row = 0;
			int lowCol = 0x00;
			int highCol = 0x11;
			if (!setHorizontalMode()){
				return false;
			}
			for (int i = 0; i < map.length; i++){
				cmd_out[0] = (SH1107G_Consts.SET_ROW_BASE_BYTE + row);
				cmd_out[1] = lowCol;
				cmd_out[2] = highCol;	
				sendCommands(0,3);
				int curByte = map[i];
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
			return sendData(0, map.length);

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
	public boolean display(int[][] raw_image) {
		return display(raw_image, 4);
	}

	
	/**
	 * Displays 96x96 integer array.
	 * @param raw_image
	 * @param pixelDepth is the pixelDepth in bits of the input; this information allows the method to automatically
	 *  convert to 4-bit pixelDepth as spec'ed by the physical hardware.
	* @return true if the channel was ready for the i2c commands.
	 */
	@Override
	public boolean display(int[][] raw_image, int pixelDepth){
		switch (chip){
		case SSD1327:
			if (!setHorizontalMode()){
				return false;
			}
			int index = 0;
			int mask = (1 << pixelDepth) - 1;
			for (int i = 0; i < OLED_96x96_Consts.ROW_COUNT; i ++){
				for (int j = 0; j < OLED_96x96_Consts.COL_COUNT; j = j + 2){
					int b = (raw_image[i][j] & mask) << 4;
					b |= raw_image[i][j+1] & mask;
					data_out[index++] = b;
				}
			}
			return sendData(0,index);
		case SH1107G:
			//TODO: implement function for SH1107G chip
			return false;
		default:
			return false;

		}

	}


	@Override
	public boolean setHorizontalMode() {
		switch(chip){
		case SSD1327:
			cmd_out[0] = SSD1327_Consts.REMAP;
			cmd_out[1] = SSD1327_Consts.HORIZONTAL;
			cmd_out[2] = SSD1327_Consts.SET_ROW_ADDRESS;
			cmd_out[3] = 0;
			cmd_out[4] = 95;
			cmd_out[5] = SSD1327_Consts.SET_COL_ADDRESS;
			cmd_out[6] = 8; //the 8th column on the chip corresponds to the 0th column on the actual screen
			cmd_out[7] = 47 + 8;//end at col + 47th column, again offset by 8.
			return sendCommands(0,8);


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
			cmd_out[0] = SSD1327_Consts.REMAP;
			cmd_out[1] = SSD1327_Consts.VERTICAL;

			break;
		case SH1107G:
			cmd_out[0] = SH1107G_Consts.REMAP_SGMT;
			cmd_out[1] = SH1107G_Consts.SET_VERTICAL;
			break;
		default:
			return false;
		}

		return sendCommands(0,2);
	}
	/*
	/**
	 * Overrides the base implementation and sends a "DATA_MODE" identifier byte before every single data byte.
	 * @param data
	 * @param start
	 * @param length
	 * @param finalTargetIndex
	 * @return true if the data is sent; false otherwise.
	 */
	/*
	@Override
	protected boolean sendData(int [] data, int start, int length, int finalTargetIndex){
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		length = length / 2; //we need to send two bytes for each command
		int i;
		for (i = start; i < Math.min(start + length, finalTargetIndex); i++){
			i2cPayloadWriter.write(DATA_MODE);
			i2cPayloadWriter.write(data[i]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		if (i == finalTargetIndex){
			return true;
		}
		return sendData(data, i, BATCH_SIZE, finalTargetIndex); //calls itself recursively until we reach finalTargetIndex
	}
	 */

	public void setClearScreenUponStartup(boolean clear){
		clearScreenUponStartup = clear;
	}

	
	@Override
	public void startup() {
		logger.info("OLED_96x96 initialized and cleared.");
		this.init();
		if (clearScreenUponStartup){
			this.clear();
		}
	}
}
