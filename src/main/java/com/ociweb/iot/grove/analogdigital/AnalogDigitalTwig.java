package com.ociweb.iot.grove.analogdigital;


import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay.*;
import com.ociweb.iot.grove.four_digit_display.FourDigitDisplayFacade;

import com.ociweb.iot.grove.four_digit_display.FourDigitDisplayFacade;

/**
 * Holds information for all standard Analog and Digital I/O twigs in the Grove starter kit.
 *
 * Methods are necessary for interpreting new connections declared in
 * IoTSetup declareConnections in the maker app.
 *
 * @see com.ociweb.iot.hardware.IODevice
 */
//TODO: Moved everything out to either analog or digital. These two are left here because they are odd and we need to decide 
//	where to put them.
@Deprecated
public enum AnalogDigitalTwig implements ADIODevice {
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
	
	
	
	

	
	
	FourDigitDisplay(){
		;
		@Override
		public boolean isOutput(){
			return true;
		}

		@Override
		public int pinsUsed(){
			return 2;
		}
		@Override
		public boolean isI2C(){
			return true;
		}

		@Override
		public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
			byte[] read_cmd = {};
			byte[] set_up = {GROVE_TM1637_INIT, 5, 0x00,0x00}; //default to digit output 5
			byte address = 0x4;
			byte bytes_to_read = 0;
			byte reg = 0;
			return new I2CConnection(this, address, read_cmd, bytes_to_read, reg, set_up);
		}

		@Override
		public byte[] I2COutSetup() {
			byte [] set_up = {GROVE_TM1637_INIT, 5, 0x00,0x00};
			return set_up;
		}
		
	};

	

	

	
	
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
	public int response() {
		return 20;
	}

	/**
	 * @return Delay, in milliseconds, for scan. TODO: What's scan?
	 */
	public int scanDelay() {
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

	public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
		return null;
	}
}
