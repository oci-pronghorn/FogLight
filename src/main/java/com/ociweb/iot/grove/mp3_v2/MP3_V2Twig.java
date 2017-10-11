package com.ociweb.iot.grove.mp3_v2;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.SerialIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;


/**
 * 
 * @author Ray Lo, raylo@wustl.edu
 *
 */
public enum MP3_V2Twig implements SerialIODevice{
	
	MP3_V2(){
		@Override 
		public boolean isOutput(){
			return true;
		}
		@Override 
		public int defaultPullRateMS(){
			return 40;
		}
		@Override
		public boolean isValid(byte[] backing, int position, int length, int mask){
			return true;
		}
		
		@Override
		public MP3_V2_Transducer newTransducer(FogCommandChannel... ch) {
			return new MP3_V2_Transducer(ch[0]);
			
		}
		
	};

	@Override
	public int defaultPullRateMS() {
		return 0;
	}

	@Override
	public int pullResponseMinWaitNS() {
		return 0;
	}

	@Override
	public boolean isInput() {		
		return false;
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

	@Override
	public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
		return null;
	}

}
