package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public class UltrasonicRangerTwig implements IODevice{

	public byte addr = 0x52;
	private byte connection;
	public UltrasonicRangerTwig(byte connection) {
		this.connection = connection;
	}

	@Override
	public I2CConnection getI2CConnection(){
		byte[] URANGE_READCMD = {0x01, 0x07, connection, 0x00, 0x00};
	    byte[] URANGE_SETUP = null;
	    byte URANGE_ADDR = 0x04;
	    byte URANGE_BYTESTOREAD = 2;
	    byte URANGE_REGISTER = connection;
	    return new I2CConnection(this, URANGE_ADDR, URANGE_READCMD, URANGE_BYTESTOREAD, URANGE_REGISTER, URANGE_SETUP);
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
	public boolean isGrove() {
		return false;
	}

}