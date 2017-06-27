package com.ociweb.iot.grove.OLED.OLED_128x64;

import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.*;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Direction.Left;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Direction.Right;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Orientation.Vertical_Left;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Orientation.Vertical_Right;

import com.ociweb.iot.grove.OLED.OLED_DataAndCommandsSender;
import com.ociweb.iot.grove.OLED.ScrollSpeed;
import com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Direction;
import com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.Orientation;
import com.ociweb.iot.maker.FogCommandChannel;

/**
 * IODevice object that holds on to the FogCommandChannel, data_output array, and cmd_output array needed for the static Grove_OLED_128x64 utility class.
 * @author Ray Lo, Nathan Tippy
 *
 */
public class OLED_128x64 extends OLED_DataAndCommandsSender{
	
	/**
	 * Constructs an instance of OLED_128x64 that holds on to the {@link FogCommandChannel} passed in.
	 * @param ch
	 */
	
	public OLED_128x64(FogCommandChannel ch){
		super(ch, new int[1024], new int[32], OLEDADDRESS);
		//the most amount of data we can ever send at once as this is one entire frame worth of data
		//the static Grove_OLED_128x64 class requires that we send out no more than 10 bytes at once. 32 bytes are allocated for safety.
	}
	
	/**
	 * Flashes the display screen off and then on and ensures that the inverse_display and scrolling functions
	 *  are turned off. The display is left in the Page mode afterwards.
	 * @return true if the commands were sent, returns false if any single command was not sent.
	 */
	@Override
	public boolean init(){
		cmd_out[0] = PUT_DISPLAY_TO_SLEEP;
		cmd_out[1] = WAKE_DISPLAY;
		cmd_out[2] = TURN_OFF_INVERSE_DISPLAY;
		cmd_out[3] = DEACTIVATE_SCROLL;
		cmd_out[4] = SET_MEMORY;
		cmd_out[5] = 0x02;
		cmd_out[6] = SET_DISPLAY_OFFSET;
		cmd_out[7] = 0x00;
		return sendCommands(0, 8);
	}

	/**
	 * Sets the contrast (olso refered to as brightness) of the display.
	 * @param contrast is a value ranging from 0 to 255. A bit-mask is enforced so that only the lowest 8 bits of the supplied integer will matter.
	 * @return true if the command byte and the contrast byte were sent, false otherwise.
	 */
	@Override
	public boolean setContrast(int contrast){
		cmd_out[0] = SET_CONTRAST_CONTROL;
		cmd_out[1] = contrast & 0xFF;
		return sendCommands(0, 2);
	}

	/**
	 * Sets the display in page mode, necessary for {@link #setTextRowCol(int,int)}, {@link #printCharSequence(CharSequence)} . 
	 * @return true if all three bytes needed were sent, false otherwise.
	 * @see <a href = "https://github.com/SeeedDocument/Grove_OLED_Display_0.96/raw/master/resource/SSD1308_1.0.pdf">SSD1308.pdf</a>
	 *for information on page mode.
	 */
	public boolean setPageMode(){		
		cmd_out[0] = SET_MEMORY;
		cmd_out[1] = 0x02;
		return sendCommands(0, 2);
	}

	/**
	 * Sets the display in horizontal mode, necessary for {drawBitmap(FogCommandChannel, int[], int[])} and {displayImage(int[][])}
	 * Note that both {drawBitmap(FogCommandChannel, int[], int[])} and {displayImage(int[][])} already automatically set the display in
	 * horizontal mode.
	 * @return true if all three bytes needed were sent, false otherwise.
	 * @see <a href = "https://github.com/SeeedDocument/Grove_OLED_Display_0.96/raw/master/resource/SSD1308_1.0.pdf">SSD1308.pdf</a>
	 * for information on horizontal mode. 
	 */

	public boolean setHorizontalMode(){
		cmd_out[0] = SET_MEMORY;
		cmd_out[1] = 0x00;
		return sendCommands(0, 2);
	}

	/**
	 * Sets the display in vertical mode.
	 * @return true if all three bytes needed were sent, false otherwise. 
	 * @see <a href = "https://github.com/SeeedDocument/Grove_OLED_Display_0.96/raw/master/resource/SSD1308_1.0.pdf">SSD1308.pdf</a>
	 * for information on vertical mode.
	 */

	public boolean setVerticalMode(){
		cmd_out[0] = SET_MEMORY;
		cmd_out[1] = 0x01;
		return sendCommands(0, 2);
	}

	/**
	 * Turns on the inverse feature which switches all black pixels with white pixels and vice versa.
	 * @return true if all two necessary bytes were sent, false otherwise.
	 */
	@Override
	public boolean inverseOn(){
		return sendCommand(TURN_ON_INVERSE_DISPLAY);
	}

	/**
	 * Turns off the inverse feature which switches all black pixels with white pixels and vice versa.
	 * @return true if all two necessary bytes were sent, false otherwise.
	 */
	@Override
	public boolean inverseOff(){
		return sendCommand(TURN_OFF_INVERSE_DISPLAY);
	}

	/**
	 * Activates the scroll feature.
	 * NOTE: One of the four set-up methods ({setUpRightContinuousVerticalHorizontalScroll(ScrollSpeed, int, int, int)},
	 * {setUpLeftContinuousVerticalHorizontalScroll(ScrollSpeed, int, int, int)}, {setUpRightContinuousHorizontalScroll(ScrollSpeed, int, int)},
	 * and {setUpLeftContinuousVerticalHorizontalScroll(ScrollSpeed, int, int, int)}) needs to be invoked first.
	
	 * @return true if all two necessary bytes were sent, false otherwise.
	 * @see <a href = "https://github.com/SeeedDocument/Grove_OLED_Display_0.96/raw/master/resource/SSD1308_1.0.pdf">SSD1308.pdf</a>
	 * for information on scrolling.
	 */
	@Override
	public boolean activateScroll(){
		return sendCommand(ACTIVATE_SCROLL);
	}

	/**
	 * Deactivates the scroll feature.
	 * @return true if all two necessary bytes were sent, false otherwise.
	 * @see <a href = "https://github.com/SeeedDocument/Grove_OLED_Display_0.96/raw/master/resource/SSD1308_1.0.pdf">SSD1308.pdf</a>
	 * for information on scrolling.
	 */
	@Override
	public boolean deactivateScroll(){
		return sendCommand(DEACTIVATE_SCROLL);
	}
	
	@Override
	public boolean printCharSequenceAt(CharSequence s,int row, int col){
		return setTextRowCol(row,col) && printCharSequence(s);
	}

	@Override
	public boolean printCharSequence(CharSequence s){
		encodeCharSequence(s);
		return sendData(0, s.length()*8);
	}
	
	public boolean encodeChar(char c){
		return encodeChar(c, 0);
	}

	public boolean encodeChar(char c, int start){
		if (c > 127 || c < 32){
			//'c' has no defined font for Grove_OLED_128x64");
			return false;
		}
		int counter = 0;
		for (int i = start; i < start + 8; i++){
			data_out[i] = BASIC_FONT[c-32][counter];
			counter ++;
		}
		return true;
	}

	public boolean encodeCharSequence(CharSequence s){
		return encodeCharSequence(s, 0, s.length());
	}

	public boolean encodeCharSequence(CharSequence s, int start, int CharSequence_length){
		for (int i = start; i < start + CharSequence_length; i++){
			if (encodeChar(s.charAt(i), i*8)){	
			} else {
				return false;
			}
		}
		return true;
	}

	


	@Override
	public boolean setTextRowCol( int row, int col){ //only works in Page Mode
		//bit-mask because x and y can only be within a certain range (0-7)

		cmd_out[0] = ROW_START_ADDRESS_PAGE_MODE + (row & 0x07);
		cmd_out[1] = LOWER_COL_START_ADDRESS_PAGE_MODE + (8*col & 0x0F);
		cmd_out[2] = HIGHER_COL_START_ADDRESS_PAGE_MODE + ((8*col >> 4) & 0x0F);
		

		return sendCommands(0, 3);
	}

	public boolean setPageModeAndTextRowCol(int row, int col){
		return setPageMode() && setTextRowCol(row,col);

	}

	public boolean setDisplayStartLine(int startLine){
		cmd_out[0] =  DISPLAY_START_LINE;
		cmd_out[1] = startLine & 0x3F;
		return sendCommands(0,2);
	}

	public boolean remapSegment(boolean isRemapped){
		int remap_cmd = MAP_ADDRESS_0_TO_SEG0;
		if (isRemapped){
			remap_cmd = MAP_ADDRESS_127_TO_SEG0;
		}
		return sendCommand(remap_cmd);
	}

	
	/**
	 * Switches the display to page mode to print spaces all across the screen.
	 * @return true if the commands were sent, false otherwise.
	 */

	@Override
	public boolean clear(){
		if (setPageMode()){

		} else {
			return false;
		}

		for (int row = 0; row < 8; row++){
			setTextRowCol(row, 0);
			if (sendData(EMPTY_ROW)){
			} else {
				return false;
			}
		}
		return true;
	}

	public  boolean cleanClear(FogCommandChannel ch, int[] output){
		if (sendCommand(PUT_DISPLAY_TO_SLEEP)
				&& clear() 
				&& sendCommand(WAKE_DISPLAY))
		{
			ch.i2cFlushBatch();
			return true;
		}
		ch.i2cFlushBatch();		
		return false;
	}

	public  boolean setMultiplexRatio(int mux_ratio){
		cmd_out[0] = SET_MUX_RATIO;
		cmd_out[1] = mux_ratio & 0x3F;
		return sendCommands( 0,2);
	}

	public boolean setClockDivRatioAndOscFreq(int clock_div_ratio, int osc_freq){
		cmd_out[0] = SET_CLOCK_DIV_RATIO;
		cmd_out[1] = (clock_div_ratio & 0x0F) | (osc_freq << 4 & 0xF0);
		return sendCommands( 0,2);

	}
	public boolean setVerticalOffset(int offset){
		cmd_out[0] = SET_DISPLAY_OFFSET;
		cmd_out[1] = offset & 0x3F;
		return sendCommands( 0,2);
	}
/*
	public static boolean setUpRightContinuousHorizontalScroll(FogCommandChannel ch, ScrollSpeed speed, int startPage, int endPage, int[] output){
		return setUpContinuousHorizontalScroll(ch, speed, startPage, endPage, Right, output);
	}
	public static boolean setUpLeftContinuousHorizontalScroll(FogCommandChannel ch, ScrollSpeed speed, int startPage, int endPage, int[] output){
		return setUpContinuousHorizontalScroll(ch, speed, startPage, endPage, Left, output);
	}

	public static boolean setUpRightContinuousVerticalHorizontalScroll(FogCommandChannel ch, ScrollSpeed speed, int startPage, 
			int endPage, int offset,  int[] output){
		return setUpContinuousVerticalHorizontalScroll(ch, speed, startPage, endPage, offset, Vertical_Right, output);
	}

	public static boolean setUpLeftContinuousVerticalHorizontalScroll(FogCommandChannel ch, ScrollSpeed speed, int startPage, 
			int endPage, int offset,  int[] output){
		return setUpContinuousVerticalHorizontalScroll(ch, speed, startPage, endPage, offset, Vertical_Left, output);
	}

	
	private boolean setUpContinuousHorizontalScroll(ScrollSpeed speed, int startPage, int endPage, 
			Direction dir){	
		generateHorizontalScrollComamnds(speed,startPage,endPage,dir);
		return sendCommands(0,7);
	}
	
	private void generateHorizontalScrollComamnds(ScrollSpeed speed, int startPage, int endPage, 
			Direction dir){

		cmd_out[0] = dir == Right? SET_RIGHT_HOR_SCROLL:SET_LEFT_HOR_SCROLL;
		cmd_out[1] = 0x00; //dummy byte as required
		cmd_out[2] = startPage & 0x07;
		cmd_out[3] =speed.COMMAND;
		cmd_out[4] =endPage & 0x07;
		cmd_out[5] = 0xFF; // dummy byte as required
		cmd_out[6] = 0x00; // dummy byte as required		
	}
	
	private boolean setUpContinuousVerticalHorizontalScroll(ScrollSpeed speed, int startPage, int endPage,
			int offset, Orientation dir){
		generateVerticalHorizontalScrollComamnds(speed,startPage,endPage,offset,dir);
		return sendCommands(0,6);

	}

	private void generateVerticalHorizontalScrollComamnds(ScrollSpeed speed, int startPage, int endPage, 
			int offset, Orientation ori){
		
		cmd_out[0] = ori == Vertical_Right? SET_VER_AND_RIGHT_HOR_SCROLL:SET_VER_AND_LEFT_HOR_SCROLL;
		cmd_out[1] = 0x00; //dummy byte as required
		cmd_out[2] = startPage & 0x07;
		cmd_out[3] =speed.COMMAND;
		cmd_out[4] =endPage & 0x07;
		cmd_out[5] = offset & 0x1F;
	}
	
	/**
	 * NOTE: this method leaves the display in horizontal mode

	 * @param map
	 * @return true if the i2c commands were succesfully sent, false otherwise
	 */
	
	/*
	public boolean drawBitmapInHorizontalMode(int[] map){
		if (!setHorizontalMode()){
			return false;
		}
		return sendData(ch,map);
	}
*/
	@Override
	public boolean drawBitmap(int[] map){
		return drawBitmapInPageMode( map);
	}

	/**
	 * NOTE: drawing in page mode instead of horizontal mode sends 16 extra bytes per reflash compared to drawing
	 * in horizontal mode as we need to reset textRowCol everytime we reach a new page. It may be preferable to use
	 * drawing in page mode however, as it eliminates the need to switch between page mode and horizontal mode when doing
	 * both drawing and CharSequence printing.
	 * @return true
	 */
	
	public  boolean drawBitmapInPageMode (int[] map){
		for (int page = 0; page <8; page++){
			if (! setTextRowCol(page,0)){
				return false;
			}
			int startingPoint = page*128;

			if (!sendData(map, startingPoint, 128)){
				return false;
			}

		}
		return true;
	}

	

	
	/*
	public boolean cleanClear(){
		return Grove_OLED_128x64.cleanClear(this.ch, cmd_output);
	}
	

	*/
	public boolean displayImage(int[][] raw_image){
		int counter = 0;
		int pageLimit = row_count >> 3;
		for (int page = 0; page < pageLimit; page++){
			for (int seg = 0; seg < col_count; seg++){
				data_out[counter] = parseColByte(raw_image, page*8, seg);
				counter++;
			}
		}
		return drawBitmap(data_out);
	}


	private static int parseColByte(int[][]raw_image, int row, int col){
		int ret = 0;
		for (int i = 0; i < 8; i ++){
			ret = ret | (raw_image[row+i][col] & 0x01) << i;
		}
		return ret;
	}
	/*
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
	*/

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
	public boolean setUpScroll() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cleanClear() {
		// TODO Auto-generated method stub
		return false;
	}


	

}
