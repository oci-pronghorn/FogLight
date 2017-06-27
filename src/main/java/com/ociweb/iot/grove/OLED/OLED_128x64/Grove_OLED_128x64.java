package com.ociweb.iot.grove.OLED.OLED_128x64;

import com.ociweb.iot.hardware.I2CConnection;

import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;


/**
 * Singleton utility class that communicates with the i2c Grove OLED 128x64 display, includes basic functionality such as printing
 * bitmap or CharSequence.
 * @author Ray Lo, Nathan Tippy
 *
 */
public class Grove_OLED_128x64 implements IODevice{
	
	public static final Grove_OLED_128x64 instance = new Grove_OLED_128x64();
	
	/**
	 * Private constructor for singleton design pattern.
	 */
	private Grove_OLED_128x64(){
	}


	/**
	 * Dynamically allocates an instance of {@link OLED_128x64}
	 * @param ch {@link FogCommandChannel} reference to be held onto by the new {@link OLED_128x64}
	 * @return the new instance of {@link OLED_128x64} created.
	 */
	public static OLED_128x64 newObj(FogCommandChannel ch){
		return new OLED_128x64(ch);
	}
	
	
	
/*
	private static boolean iterativeSendData(FogCommandChannel ch, int[] data, int start, int length){
		
		 * 
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		int counter = 1;
		i2cPayloadWriter.write(DATA_MODE);

		//TODO: Fix for loop to not check batch size every iteration
		for (int i = start; i < start+length; i ++){
			if (counter < BATCH_SIZE){	
				i2cPayloadWriter.write(data[i]);
				counter = counter + 1;
			}
			else {
				ch.i2cCommandClose();
				ch.i2cFlushBatch();

				i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
				i2cPayloadWriter.write(DATA_MODE);
				i2cPayloadWriter.write(data[i]);
				counter = 2;
			}
		}
		if (counter > 0){
			ch.i2cCommandClose();
			ch.i2cFlushBatch();
		}
		 */
		/*
		final int num_batches = length / BATCH_SIZE;
		DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
		i2cPayloadWriter.write(DATA_MODE);

		//iterate through all the batches we need to send

		for (int i = 0; i < num_batches; i++){
			//iterate through all the indiviusal bytes in each batch
			for (int j = start + (i * BATCH_SIZE); j < start + ( (i + 1) * BATCH_SIZE); j++){
				i2cPayloadWriter.write(data[j]);
			}
			//flush the batch once a batch is full
			ch.i2cCommandClose();
			ch.i2cFlushBatch();
			i2cPayloadWriter = ch.i2cCommandOpen(OLEDADDRESS);
			i2cPayloadWriter.write(DATA_MODE);
		}

		//send the last fraction of batch worth of remaining bytes.
		int start_last_batch = start + (num_batches * BATCH_SIZE);

		for (int j = start_last_batch; j < start + length; j++){
			i2cPayloadWriter.write(data[j]);
		}
		ch.i2cCommandClose();
		ch.i2cFlushBatch();

		return true;
		}
		 */


	
	

	


	@Override
	public int response() {
		return 20;
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
		byte[] LED_READCMD = {};
		byte[] LED_SETUP = {};
		byte LED_ADDR = 0x04;
		byte LED_BYTESTOREAD = 0;
		byte LED_REGISTER = 0;
		return new I2CConnection(this, LED_ADDR, LED_READCMD, LED_BYTESTOREAD, LED_REGISTER, LED_SETUP);
	}

	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return true;
	}

	@Override
	public int pinsUsed() {
		return 1;
	}

}
