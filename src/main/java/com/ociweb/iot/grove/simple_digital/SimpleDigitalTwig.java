package com.ociweb.iot.grove.simple_digital;

import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;


/**
 * 
 * @author Ray Lo
 *
 */
public enum SimpleDigitalTwig implements ADIODevice {
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
		public int defaultPullRateMS(){
			return 60;
		}
	},
	Button() {
		@Override
		public boolean isInput() {
			return true;
		}

		public int defaultPullRateMS() {
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
	RotaryEncoder() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int pinsUsed() {
			return 2;
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
	MDDS30Power() {
		@Override
		public boolean isOutput() {
			return true;
		}

		@Override
		public boolean isPWM() {
			return true;
		}
		
		@Override
		public int range() {
			return 1024;
		}
	},
	MDDS30Direction() {
		@Override
		public boolean isOutput() {
			return true;
		}

		@Override
		public boolean isPWM() {
			return false;
		}
		
		@Override
		public int range() {
			return 1;
		}
	},
	Relay() {
		@Override
		public boolean isOutput() {
			return true;
		}
	},
	;
	/**
	 * @return True if this twig is an input device, and false otherwise.
	 */
	public boolean isInput() {
		return false;
	}

	/**
	 * @return True if this twig is an output device, and false otherwise.
	 */
	public boolean isOutput() {
		return false;
	}

	/**
	 * @return Response time, in milliseconds, for this twig.
	 */
	public int defaultPullRateMS() {
		return 20;
	}

	/**
	 * @return Wait, in nanoseconds, for response.
	 */
	public int pullResponseMinWaitNS() {
		return 0;
	}

	/**
	 * @return True if this twig is Pulse Width Modulated (PWM) device, and
	 *         false otherwise.
	 */
	public boolean isPWM() {
		return false;
	}

	/**
	 * @return True if this twig is an I2C device, and false otherwise.
	 */
	public boolean isI2C() {
		return false;
	}

	/**
	 * @return The {@link I2CConnection} for this twig if it is an I2C
	 *         device, as indicated by {@link #isI2C()}.
	 */
	public I2CConnection getI2CConnection() {
		return null;
	}

	/**
	 * @return The possible value range for reads from this device (from zero).
	 */
	public int range() {
		return 256;
	}

	/**
	 * @return the setup bytes needed to initialized the connected I2C device
	 */
	public byte[] I2COutSetup() {
		return null;
	}

	/**
	 * Validates if the I2C data from from the device is a valid response for this twig
	 *
	 * @param backing
	 * @param position
	 * @param length
	 * @param mask
	 *
	 * @return fals if the bytes returned from the device were not some valid response
	 */
	public boolean isValid(byte[] backing, int position, int length, int mask) {
		return true;
	}

	/**
	 * @return The number of hardware pins that this twig uses.
	 */
	public int pinsUsed() {
		return 1;
	}

	public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
		return null;
	}
	
	@Override
	public <F extends IODeviceTransducer> F newTransducer(Port p, FogCommandChannel... ch){
		return null;
	}

}
