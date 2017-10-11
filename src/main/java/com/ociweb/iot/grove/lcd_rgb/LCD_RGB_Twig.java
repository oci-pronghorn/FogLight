package com.ociweb.iot.grove.lcd_rgb;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;

public enum LCD_RGB_Twig implements I2CIODevice {
	LCD_RGB(){
		@Override
		public boolean isOutput(){
			return true;
		}
		@Override
		public LCD_RGB_Transducer newTransducer(FogCommandChannel... ch){
			return new LCD_RGB_Transducer(ch[0]);
		}
		
	}
	;

	@Override
	public int defaultPullRateMS() {
		// TODO Auto-generated method stub
		return 40;
	}

	@Override
	public int pullResponseTimeoutNS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPWM() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int range() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public I2CConnection getI2CConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int pinsUsed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
}
