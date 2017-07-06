package com.ociweb.iot.grove.oled;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

public abstract class OLED_DataAndCommandsSender {
	protected final FogCommandChannel ch;
	protected final int[] data_out;
	protected final int[] cmd_out;
	protected final int i2c_address;
	
	private static final int BATCH_SIZE = 50;
	public static final int COMMAND_MODE = 0x80;
	public static final int DATA_MODE = 0x40;
	
	protected OLED_DataAndCommandsSender(FogCommandChannel ch, int[] data_out, int[]cmd_out, int i2c_address){
		this.ch = ch;
		this.data_out = data_out;
		this.cmd_out = cmd_out;
		this.i2c_address = i2c_address;
	}
	
	/**
	 * Sends a "data" identifier byte followed by the user-supplied byte over the i2c.
	 * @return true if the command byte and the supplied byte were succesfully sent, false otherwise.
	 */

	protected boolean sendData(){
		return sendData(0, data_out.length);
	}
	
	protected boolean sendData(int[] data){
		return sendData(data, 0, data.length);
	}
	
	/**
	 * If no data is supplied, we are using the default data_out held by this object
	 * @param start
	 * @param length
	 * @return true
	 */
	protected boolean sendData(int start, int length){
		return sendData(data_out, start,length);
	}
	
	/**
	 * Send an array of data
	 * Implemented by calling {@link #sendData(int[], int, int, int)}, which recursively calls itself
	 * exactly 'm' times, where 'm' is the number of batches requires to send the data array specified by the start and length.
	 * Implemented to use an array of passed-in data instead of defaulting to this.data_out so that one doesn't have
	 * to go through the trouble of copying the entire data array if the data array is already constructed
	 * @param start
	 * @param length
	 * @return true if the i2c bus is ready, false otherwise.
	 */
	protected boolean sendData(int[] data, int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		//call the helper method to recursively send batches
		return sendData(data, start,BATCH_SIZE, start+length);
	}
	
	private boolean sendData(int [] data, int start, int length, int finalTargetIndex){
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		i2cPayloadWriter.write(DATA_MODE);
		int i;
		for (i = start; i < Math.min(start + length - 1, finalTargetIndex); i++){
			i2cPayloadWriter.write(data[i]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		if (i == finalTargetIndex){
			return true;
		}
		return sendData(data, i, BATCH_SIZE, finalTargetIndex); //calls itself recursively until we reach finalTargetIndex
	}
	
	
	protected boolean sendCommand(int b){
		if (!ch.i2cIsReady()){
			return false;
		}
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		i2cPayloadWriter.write(COMMAND_MODE);
		i2cPayloadWriter.write(b);
		ch.i2cCommandClose();

		return true;
	}
	
	/**
	 * Unliked send data, sendCommands makes the assumption that the call is not sending more than one batch worth of commands
	 *Each command  involves two bytes. So if the caller is trying to send a command array of size 5, they are really sending
	 *10 bytes.
	 * @param start
	 * @param length
	 * @return true
	 */

	protected boolean sendCommands(int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		/*
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		
		assert(length*2 <= BATCH_SIZE);
		for (int i = start; i < start + length; i++){
		
				i2cPayloadWriter.write(COMMAND_MODE);
				i2cPayloadWriter.write(cmd_out[i]);
		
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		return true;
		*/
		return sendCommands(cmd_out,start,length);
	}
	
	protected boolean sendCommands(int[] cmd, int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		//call the helper method to recursively send batches
		return sendCommands(cmd, start,BATCH_SIZE, start+length);
	}
	
	private boolean sendCommands(int [] cmd, int start, int length, int finalTargetIndex){
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(i2c_address);
		length = length / 2; //we need to send two bytes for each command
		int i;
		for (i = start; i < Math.min(start + length, finalTargetIndex); i++){
			i2cPayloadWriter.write(COMMAND_MODE);
			i2cPayloadWriter.write(cmd[i]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		if (i == finalTargetIndex){
			return true;
		}
		return sendData(cmd, i, BATCH_SIZE, finalTargetIndex); //calls itself recursively until we reach finalTargetIndex
	}
	
	public abstract boolean init();
	public abstract boolean clear();
	public abstract boolean cleanClear();
	public abstract boolean displayOn();
	public abstract boolean displayOff();
	public abstract boolean inverseOn();
	public abstract boolean inverseOff();
	public abstract boolean setContrast(int contrast);
	public abstract boolean setTextRowCol(int row, int col);
	public abstract boolean printCharSequence(CharSequence s);
	public abstract boolean printCharSequenceAt(CharSequence s, int row, int col);
	public abstract boolean drawBitmap(int[] bitmap);
	public abstract boolean activateScroll();
	public abstract boolean deactivateScroll();
	public abstract boolean setUpScroll();
	public abstract boolean displayImage(int[][] raw_image);
	public abstract boolean setHorizontalMode();
	public abstract boolean setVerticalMode();
	
}
