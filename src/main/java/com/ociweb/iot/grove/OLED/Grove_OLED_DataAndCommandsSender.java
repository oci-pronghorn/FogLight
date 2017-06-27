package com.ociweb.iot.grove.OLED;

import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.BATCH_SIZE;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.COMMAND_MODE;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.DATA_MODE;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.OLEDADDRESS;

import com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

public class Grove_OLED_DataAndCommandsSender {
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


	/**
	 * Unliked send data, sendCommands makes the assumption that the call is not sending more than one batch worth of commands
	 *Each command  involves two bytes. So if the caller is trying to send a command array of size 5, they are really sending
	 *10 bytes.
	 * @param ch
	 * @param commands
	 * @param start
	 * @param length
	 * @return true
	 */
	public static boolean sendCommands(FogCommandChannel ch, int[] commands, int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}

		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		
		assert(length*2 <= BATCH_SIZE);
		for (int i = start; i < start + length; i++){
		
				i2cPayloadWriter.write(COMMAND_MODE);
				i2cPayloadWriter.write(commands[i]);
		
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		return true;
	}
	
	
	/**
	 * Sends a "data" identifier byte followed by the user-supplied byte over the i2c.
	 * @param ch is the {@link com.ociweb.iot.maker.FogCommandChannel} in charge of the i2c connection to this OLED.
	 * @param data is the array of data to be sent in the form of an integer array (the L.S. 8 bits of each int are used.)
	 * @return true if the command byte and the supplied byte were succesfully sent, false otherwise.
	 */
	public static boolean sendData(FogCommandChannel ch, int[] data ){
		return sendData(ch, data,0, data.length);
	}

	
	/**
	 * Send an array of data
	 * Implemented by calling {@link #sendData(FogCommandChannel, int[], int, int, int)}, which recursively calls itself
	 * exactly 'm' times, where 'm' is the number of batches requires to send the data array specified by the start and length.
	 * @param ch
	 * @param data
	 * @param start
	 * @param length
	 * @return true if the i2c bus is ready, false otherwise.
	 */
	public static boolean sendData(FogCommandChannel ch, int[] data, int start, int length){
		if (!ch.i2cIsReady()){
			return false;
		}
		//call the helper method to recursively send batches
		return sendData(ch,data,start,BATCH_SIZE, start+length);
	}

	public static boolean sendData(FogCommandChannel ch, int[] data, int start, int length, int finalTargetIndex){
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		i2cPayloadWriter.write(DATA_MODE);
		int i;
		for (i = start; i < Math.min(start + length, finalTargetIndex); i++){
			i2cPayloadWriter.write(data[i]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();
		if (i == finalTargetIndex){
			return true;
		}
		return sendData(ch, data, i, BATCH_SIZE, finalTargetIndex); //calls itself recursively until we reach finalTargetIndex
	}
}
