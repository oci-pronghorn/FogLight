package com.ociweb.iot.grove.nunchuck;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;


//TODO: fix the button response
public class NunchuckTwig implements IODevice{

	public byte addr = 0x52;
	public NunchuckTwig() {
	}

	@Override
	public I2CConnection getI2CConnection(){
		byte[] NUNCHUCK_READCMD = {0x00};
	    byte[] NUNCHUCK_SETUP = {0x40, 0x00};
	    byte NUNCHUCK_ADDR = 0x52;
	    byte NUNCHUCK_BYTESTOREAD = 6;
	    byte NUNCHUCK_REGISTER = 0;
	    return new I2CConnection(this, NUNCHUCK_ADDR, NUNCHUCK_READCMD, NUNCHUCK_BYTESTOREAD, NUNCHUCK_REGISTER, NUNCHUCK_SETUP);
	}
	
	/*
	 * Interprets the data given by an I2C Event from a nunchuck.
	 * 
	 * Returns an int array length 7:
	 * [Stick X, Stick Y, Accel X, Accel Y, Accel Z, C button, Z button]
	 */
	public int[] interpretData(byte[] backing, int position, int length, int mask){
		assert(length==6) : "Non-Nunchuck data passed into the NunchuckTwig class";
		int[] temp = {0,0,0,0,0,0,0};
		for (int i = 0; i < 6; i++) {
			temp[i] = (int)((backing[(position+i)&mask] ^ 0x17) + 0x17)&0xFF;
		}
		temp[6]= 1-(temp[5]&0x01);
		temp[5]= 1-((temp[5]&0x02)>>1);
		return temp;
	}
	
	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask){
		return true; //TODO: fill in valid testing
	}

	@Override
	public boolean isInput() {
		return true;
	}

	@Override
    public int scanDelay() {
    	return 0;
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
    public int response() {       
       return 10;      
    }
	
    @Override
    public int pinsUsed() {
        return 1;
    }

	@Override
	public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
