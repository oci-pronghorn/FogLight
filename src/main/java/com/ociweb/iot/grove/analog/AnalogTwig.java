package com.ociweb.iot.grove.analog;

import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

public enum AnalogTwig implements ADIODevice{
	UVSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int response() {
			return 30;
		}


	},
	LightSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int response() {
			return 100;
		}
	},
	SoundSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int response() {
			return 2;
		}
	},
	AngleSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int response() {
			return 40;
		}
		@Override
		public int range() {
			return 1024;
		}
	},
	MoistureSensor() {
		@Override
		public boolean isInput() {
			return true;
		}
	},
	Servo() {
		@Override
		public boolean isOutput() {
			return true;
		}
	},
	UltrasonicRanger() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int range() {
			return 1024;
		}

		@Override
		public int response() {
			return 200;
		}

		@Override
		public int scanDelay() {
			return 1_420_000;
		}

	},
	WaterSensor(){
		@Override
		public boolean isInput(){
			return true;
		}

		@Override
		public int range(){
			return 1024;
		}
	},
	VibrationSensor(){
		@Override
		public boolean isInput(){
			return true;
		}
		@Override
		public int range(){
			return 1024;
		}
	};
	;
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
