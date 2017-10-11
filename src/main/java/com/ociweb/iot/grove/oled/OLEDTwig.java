package com.ociweb.iot.grove.oled;
import com.ociweb.iot.grove.oled.oled2.OLED96x96Transducer;
import com.ociweb.iot.hardware.I2CConnection;

import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;

/**
 * 
 * @author Ray Lo, raylo@wustl.edu
 *
 */
public enum OLEDTwig implements I2CIODevice {
	
	OLED_128x64(){
		@Override
		public boolean isOutput(){
			return true;
		}
		@SuppressWarnings("unchecked")
		@Override
		public OLED_128x64_Transducer newTransducer(FogCommandChannel... ch) {
			return new OLED_128x64_Transducer(ch[0]);
		}
	},

	OLED_96x96_2() {
		@Override
		public boolean isOutput() {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public OLED96x96Transducer newTransducer(FogCommandChannel... ch) {
			return new OLED96x96Transducer(ch[0]);//TODO:feed the right chip enum, create two seperate twigs
		}
	},
	
	OLED_96x96(){
		@Override
		public boolean isOutput(){
			return true;
		}
		@SuppressWarnings("unchecked")
		@Override
		public OLED_96x96_Transducer newTransducer(FogCommandChannel...ch){
			return new OLED_96x96_Transducer(ch[0]);//TODO:feed the right chip enum, create two seperate twigs
		}	
	};

	@Override
	public int defaultPullRateMS() {
		// TODO Auto-generated method stub
		return 0;
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
