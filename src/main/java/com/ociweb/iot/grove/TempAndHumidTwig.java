package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public class TempAndHumidTwig implements IODevice{

	public byte addr = 0x04;
	public TempAndHumidTwig() {
	}

	public I2CConnection getI2CConnection(byte connection, byte module_type){
		byte[] TEMPHUMID_READCMD = {0x01, 0x40, connection, module_type, 0x00};
	    byte[] TEMPHUMID_SETUP = {0x01, 0x05, connection, 0x00, 0x00}; //set pinMode to output
	    byte TEMPHUMID_ADDR = 0x04;
	    byte TEMPHUMID_BYTESTOREAD = 4;
	    byte TEMPHUMID_REGISTER = connection;
	    return new I2CConnection(this, TEMPHUMID_ADDR, TEMPHUMID_READCMD, TEMPHUMID_BYTESTOREAD, TEMPHUMID_REGISTER, TEMPHUMID_SETUP);
	}
	
	public byte[] interpretData(int register, long time, byte[] backing, int position, int length, int mask){
		assert(length==6) : "Non-Nunchuck data passed into the NunchuckTwig class";
		assert(register == 0) : "Non-Nunchuck data passed into the NunchuckTwig class";
		byte[] temp = {0,0,0,0,0,0};
		for (int i = 0; i < 5; i++) {
			temp[i] = (byte) ((backing[(position+i)&mask] ^ 0x17) + 0x17);
		}
		temp[5] = backing[(position+5)&mask];
		return temp;
	}
	
	@Override
	public void writeBit(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int bitValue) {
		throw new UnsupportedOperationException();	
	}

	@Override
	public void writeInt(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int intValue, int average) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeRotation(Pipe<GroveResponseSchema> responsePipe, int connector, long time, int value, int delta,
			int speed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInput() {
		return true;
	}

	@Override
	public boolean isOutput() {
		return false;
	}

	@Override
	public boolean isPWM() {
		return false;
	}

	@Override
	public int pwmRange() {
		return 0;
	}

	@Override
	public boolean isI2C() {
		return true;
	}

	@Override
	public byte[] getReadMessage() {
		return null;
	}

	@Override
	public boolean isGrove() {
		return false; //this has to be false
	}

}
