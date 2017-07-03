package com.ociweb.iot.grove;
import com.ociweb.iot.grove.OLED.OLED_128x64.OLED_128x64_Facade;
import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;
import com.ociweb.iot.grove.RTC.Grove_RTC_Constants;
import com.ociweb.iot.grove.accelerometer.Accelerometer_16g;
import com.ociweb.iot.grove.accelerometer.Grove_Acc_Constants;
import com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor_Constants;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.hardware.IODevice;
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
	RTC(){
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public boolean isOutput() {
			return true;
		}
		@Override
		public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
			byte[] ACC_READCMD = {Grove_RTC_Constants.TIME_REG};
			//byte[] ACC_SETUP = {ADXL345_POWER_CTL,0x08};
			byte[] ACC_SETUP = {};
			byte ACC_ADDR = Grove_RTC_Constants.DS1307_I2C_ADDRESS;
			byte ACC_BYTESTOREAD = 7;
			byte ACC_REGISTER = Grove_RTC_Constants.TIME_REG; //just an identifier
			return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
		}


		@Override
		public int response() {
			return 1000;

		}
		//        @SuppressWarnings("unchecked")
		//            @Override
		//            public <F extends IODeviceFacade> F newFacade(FogCommandChannel...ch){
		//                return (F) new RTC(ch[0]);
		//            }
	},ThreeAxis_Accelerometer_16G(){
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public boolean isOutput() {
			return true;
		}

		@Override
		public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
			byte[] ACC_READCMD = {Grove_Acc_Constants.ADXL345_DATAX0};
			//byte[] ACC_SETUP = {ADXL345_POWER_CTL,0x08};
			byte[] ACC_SETUP = {};
			byte ACC_ADDR = Grove_Acc_Constants.ADXL345_DEVICE;
			byte ACC_BYTESTOREAD = 6;
			byte ACC_REGISTER = Grove_Acc_Constants.ADXL345_DATAX0; //just an identifier
			return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
		}


		@Override
		public int response() {
			return 1000;
		}
		@SuppressWarnings("unchecked")
		@Override
		public <F extends IODeviceFacade> F newFacade(FogCommandChannel...ch){
			return (F) new Accelerometer_16g(ch[0]);
		}

	},
	Mini_I2C_Motor(){
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public boolean isOutput() {
			return true;
		}
		@Override
		public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
			byte[] MOTOR_READCMD = {Grove_Mini_I2CMotor_Constants.FAULT_REG};
			byte[] MOTOR_SETUP = {};
			byte MOTOR_ADDR = Grove_Mini_I2CMotor_Constants.CH1_ADD;
			byte MOTOR_BYTESTOREAD = 1;
			byte MOTOR_REGISTER = Grove_Mini_I2CMotor_Constants.FAULT_REG;  //register identifier
			return new I2CConnection(this, MOTOR_ADDR, MOTOR_READCMD, MOTOR_BYTESTOREAD, MOTOR_REGISTER, MOTOR_SETUP);
		}

		@Override
		public int response() {
			return 1000;
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
