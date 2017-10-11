package com.ociweb.iot.grove.simple_analog;

import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;

public enum SimpleAnalogTwig implements ADIODevice{
	UVSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int defaultPullRateMS() {
			return 30;
		}


	},
	LightSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int defaultPullRateMS() {
			return 100;
		}
	},
	SoundSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int defaultPullRateMS() {
			return 2;
		}
	},
	AngleSensor() {
		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public int defaultPullRateMS() {
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
		public int defaultPullRateMS() {
			return 200;
		}

		@Override
		public int pullResponseTimeoutNS() {
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
	public int pullResponseTimeoutNS() {
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
