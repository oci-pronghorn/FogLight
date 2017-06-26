package com.ociweb.iot.grove.obj;

import static com.ociweb.iot.grove.util.Grove_OLED_128x64_Constants.*;

import com.ociweb.iot.grove.util.Grove_OLED_128x64;
import com.ociweb.iot.grove.util.ScrollSpeed;
import com.ociweb.iot.maker.FogCommandChannel;

/**
 * IODevice object that holds on to the FogCommandChannel, data_output array, and cmd_output array needed for the static Grove_OLED_128x64 utility class.
 * @author Ray Lo, Nathan Tippy
 *
 */
public class OLED_128x64{
	private final FogCommandChannel ch;
	private final int[] data_output = new int[1024]; //the most amount of data we can ever send at once as this is one entire frame worth of data
	private final int[] cmd_output = new int[32]; // the static Grove_OLED_128x64 class requires that we send out no more than 10 bytes at once. 32 bytes are allocated for safety.
	
	
	/**
	 * Constructs an instance of OLED_128x64 that holds on to the {@link FogCommandChannel} passed in.
	 * @param ch
	 */
	
	public OLED_128x64(FogCommandChannel ch){
		this.ch = ch;
	}
	
	/**
	 * Flashes the display screen off and then on and ensures that the inverse_display and scrolling functions
	 *  are turned off. The display is left in the Page mode afterwards.
	 *  Implementation: calls {@link Grove_OLED_128x64#init(FogCommandChannel, int[])}}
	 * @return true if the commands were sent, returns false if any single command was not sent.
	 */
	public boolean init(){
		return Grove_OLED_128x64.init(this.ch, cmd_output);
	}
	
	/**
	 * Switches the display to page mode to print spaces all across the screen.
	 * Implementation: calls {@link Grove_OLED_128x64#clear(FogCommandChannel, int[])}}
	 * @return true if the commands were sent, false otherwise.
	 */
	public boolean clear(){
		return Grove_OLED_128x64.clear(this.ch, cmd_output);
	}
	
	/**
	 * Turns off the screen before clearing. Turns the blank screen back on once done clearing.
	 * Implementation: calls {@link Grove_OLED_128x64#cleanClear(FogCommandChannel, int[])}
	 * @return true if the commands were sent, false otherwise.
	 */
	public boolean cleanClear(){
		return Grove_OLED_128x64.cleanClear(this.ch, cmd_output);
	}
	
	/**
	 * 
	 * @param row
	 * @param col
	 * @return true TODO
	 */
	public boolean setTextRowCol(int row, int col){
		return Grove_OLED_128x64.setTextRowCol(this.ch, row, col, cmd_output);
	}
	public boolean printCharSequence(CharSequence s){
		return Grove_OLED_128x64.printCharSequence(this.ch, s , data_output);
	}
	
	public boolean printCharSequenceAt(CharSequence s, int row, int col){
		return Grove_OLED_128x64.printCharSequenceAt(this.ch, s, data_output, row, col, cmd_output);
	}
	
	public boolean displayImage(int[][] raw_image){
		int counter = 0;
		int pageLimit = row_count >> 3;
		for (int page = 0; page < pageLimit; page++){
			for (int seg = 0; seg < col_count; seg++){
				data_output[counter] = parseColByte(raw_image, page*8, seg);
				counter++;
			}
		}
		return Grove_OLED_128x64.drawBitmap(ch, data_output, cmd_output);
	}
	
	public boolean drawBitmap(int[] bitmap){
		return Grove_OLED_128x64.drawBitmap(this.ch, bitmap, cmd_output);
	}
	

	private static int parseColByte(int[][]raw_image, int row, int col){
		int ret = 0;
		for (int i = 0; i < 8; i ++){
			ret = ret | (raw_image[row+i][col] & 0x01) << i;
		}
		return ret;
	}
	
	public boolean turnOnInverseDisplay(){
		return Grove_OLED_128x64.turnOnInverseDisplay(this.ch);
	}
	
	public boolean turnOffInverseDisplay(){
		return Grove_OLED_128x64.turnOffInverseDisplay(this.ch);
	}
	
	public boolean setPageMode(){
		return Grove_OLED_128x64.setPageMode(ch, cmd_output);
	}
	
	public boolean setHorizontalMode(){
		return Grove_OLED_128x64.setHorizontalMode(ch, cmd_output);
	}
	public boolean setVerticalMode(){
		return Grove_OLED_128x64.setVerticalMode(ch, cmd_output);
	}
	
	public boolean activateScroll(){
		return Grove_OLED_128x64.activateScroll(this.ch);
	}
	
	public boolean deactivateScroll(){
		return Grove_OLED_128x64.deactivateScroll(this.ch);
	}
	
	public boolean setMultiplexRatio(int mux_ratio){
		return Grove_OLED_128x64.setMultiplexRatio(this.ch, mux_ratio, cmd_output);
	}
	
	public boolean setClockDivRatioAndOscFreq(int clock_div_ratio, int osc_freq){
		return Grove_OLED_128x64.setClockDivRatioAndOscFreq(ch, clock_div_ratio, osc_freq, cmd_output);
	}

	public boolean setVerticalOffset(int offset){
		return Grove_OLED_128x64.setVerticalOffset(this.ch, offset, cmd_output);
	}
	
	public boolean setUpRightContinuousHorizontalScroll(ScrollSpeed speed, int startPage, int endPage){
		return Grove_OLED_128x64.setUpRightContinuousHorizontalScroll(this.ch,speed,startPage,endPage, cmd_output);
		
	}
	public boolean setUpLeftContinuousHorizontalScroll(ScrollSpeed speed, int startPage, int endPage){
		return Grove_OLED_128x64.setUpLeftContinuousHorizontalScroll(this.ch,speed,startPage,endPage, cmd_output);		
	}
	
	public boolean setUpRightContinuousVerticalHorizontalScroll(ScrollSpeed speed, int startPage, 
			int endPage, int offset){
		return Grove_OLED_128x64.setUpRightContinuousVerticalHorizontalScroll(ch, speed, startPage, endPage, offset, cmd_output);
	}
	
	public boolean setUpLeftContinuousVerticalHorizontalScroll(ScrollSpeed speed, int startPage, 
			int endPage, int offset){
		return Grove_OLED_128x64.setUpLeftContinuousVerticalHorizontalScroll(ch, speed, startPage, endPage, offset, cmd_output);
	}
	
	public boolean remapSegment(boolean isRemapped){
		return Grove_OLED_128x64.remapSegment(this.ch, isRemapped);
	}
}
