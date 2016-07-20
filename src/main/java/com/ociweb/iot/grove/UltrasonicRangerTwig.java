package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;

public class UltrasonicRangerTwig implements IODevice{

	public byte addr = 0x52;
	private byte connection;
	public UltrasonicRangerTwig(int connection) {
		this.connection = (byte) connection;
	}

	@Override
	public I2CConnection getI2CConnection(){
		byte[] URANGE_READCMD = {0x01, 0x07, connection, 0x00, 0x00};
	    byte[] URANGE_SETUP = {};
	    byte URANGE_ADDR = 0x04;
	    byte URANGE_BYTESTOREAD = 3;
	    byte URANGE_REGISTER = connection;
	    return new I2CConnection(this, URANGE_ADDR, URANGE_READCMD, URANGE_BYTESTOREAD, URANGE_REGISTER, URANGE_SETUP);
	}
	
	public int interpretData(byte[] backing, int position, int length, int mask){
		assert(length==3) : "Non-Ultrasonic data passed into the NunchuckTwig class";
		byte[] temp = {0,0,0};
		for (int i = 0; i < 3; i++) {
			temp[i] = (byte) backing[(position+i)&mask];
		}
		return (((int)temp[1])<<8) | (0xFF&((int)temp[2]));
	}
	
	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask){
		byte[] temp = {0,0,0};
		for (int i = 0; i < 3; i++) {
			temp[i] = (byte) backing[(position+i)&mask];
		}
		return length == 3 && temp[0]!=-1;
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
	public int range() {
		return 0;
	}


	@Override
	public boolean isGrove() {
		return false;
	}

    @Override
    public int response() {       
       return 100;      
    }
}