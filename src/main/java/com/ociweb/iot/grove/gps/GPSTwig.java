package com.ociweb.iot.grove.gps;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.SerialIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

public enum GPSTwig implements SerialIODevice{
	GPS (){
		@Override
		public int response(){
			return 1000;
		}
		@Override
		public boolean isInput(){
			return true;
		}
		@Override
		public boolean isOutput(){
			return true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public GPS_Facade newFacade(FogCommandChannel... ch) {
			return new GPS_Facade(ch[0]);
		}
		
	};
	@Override
	public int response() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scanDelay() {
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
	public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
}
