package com.ociweb.iot.grove.OLED.OLED_96x96;

import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.BATCH_SIZE;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.COMMAND_MODE;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.OLEDADDRESS;

import com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

public class Grove_OLED_96x96 implements IODevice {
	
	
	
	
	
	
	private static boolean sendCommand(FogCommandChannel ch, int b){
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
	 * @return
	 */
	private static boolean sendCommands(FogCommandChannel ch, int[] commands, int start, int length){
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
	
	@Override
	public int response() {		
		return 0;
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
		return null;
	}

	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return false;
	}

	@Override
	public int pinsUsed() {
		return 0;
	}

}
