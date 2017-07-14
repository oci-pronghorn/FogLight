package com.ociweb.iot.grove.digital;

import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

public enum DigitalTwig implements ADIODevice {
	Buzzer() {
		@Override
		public boolean isOutput() {
			return true;
		}

	},
	TouchSensor() {
		@Override()
		public boolean isInput(){
			return true;
		}

		@Override
		public int range(){
			return 1;
		}

		@Override
		public int response(){
			return 60;
		}
	},
	Button() {
		@Override
		public boolean isInput() {
			return true;
		}

		public int response() {
			return 40;
		}

		@Override
		public int range() {
			return 1;
		}

	},
	MotionSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int range() {
			return 1;
		}
	},
	LineFinder() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int range() {
			return 1;
		}
	},
	LED() {
		@Override
		public boolean isOutput() {
			return true;
		}

		@Override
		public boolean isPWM() {
			return true;
		}
	},
	Relay() {
		@Override
		public boolean isOutput() {
			return true;
		}
	},
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
