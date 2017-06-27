package com.ociweb.iot.grove.OLED.OLED_96x96;

import static com.ociweb.iot.grove.OLED.OLED_96x96.Grove_OLED_96x96_Constants.*;

import com.ociweb.iot.grove.OLED.OLED_DataAndCommandsSender;
import com.ociweb.iot.maker.FogCommandChannel;


public class OLED_96x96 extends OLED_DataAndCommandsSender{
	
	public OLED_96x96(FogCommandChannel ch){
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printCharSequenceAt(CharSequence s, int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawBitmap(int[] bitmap) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inverseOff() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean displayImage(int[][] raw_image) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setHorizontalMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setVerticalMode() {
		// TODO Auto-generated method stub
		return false;
	}
}
