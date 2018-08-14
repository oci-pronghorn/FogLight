package com.ociweb.iot.grove.thumb_joystick;

import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;

public enum ThumbJoystickTwig implements ADIODevice{
	ThumbJoystick(){
		@Override
		public boolean isInput(){
			return true;
		}
		@Override
		public int range(){
			return 1024;
		}
		
		@Override
		public int pinsUsed(){
			return 2;
		}
		
		@Override
		public ThumbJoystickTransducer newTransducer(FogCommandChannel... ch){
			return new ThumbJoystickTransducer();
		}
		
		@Override
		public ThumbJoystickTransducer newTransducer(Port p, FogCommandChannel... ch) {
			return new ThumbJoystickTransducer(p);
		}
		@Override
		public int defaultPullRateMS(){
			return 40;
		}
	},
	;

	@Override
	public int defaultPullRateMS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int pullResponseMinWaitNS() {
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
