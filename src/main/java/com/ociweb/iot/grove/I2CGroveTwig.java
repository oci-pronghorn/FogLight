package com.ociweb.iot.grove;
import com.ociweb.iot.grove.OLED.OLED_128x64.OLED_128x64_Facade;
import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;


public enum I2CGroveTwig implements I2CIODevice {
	OLED_128x64(){
		@Override
		public boolean isOutput(){
			return true;
		}
		@SuppressWarnings("unchecked")
		@Override
		public OLED_128x64_Facade newFacade(FogCommandChannel... ch) {
			return new OLED_128x64_Facade(ch[0]);
		}
	},

	OLED_96x96(){
		@Override
		public boolean isOutput(){
			return true;
		}
		@SuppressWarnings("unchecked")
		@Override
		public OLED_96x96_Facade newFacade(FogCommandChannel...ch){
			return new OLED_96x96_Facade(ch[0]);//TODO:feed the right chip enum, create two seperate twigs
		}
	},
        I2CMotorDriver(){
          @Override
          public boolean isOutput(){
              return true;
          }
        },
	I2C() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public boolean isOutput() {
			return true;
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
