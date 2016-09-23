package com.ociweb.iot.hardware;

/**
 * Interface for all Input and Output devices.
 */
public interface IODevice {

	/**
	 * Time in ms between read request and read response for I2C devices.
	 * @return
	 */
     public int           response(); //in ms, do not poll faster than this
     public boolean       isInput();
     public boolean       isOutput();
     public boolean       isPWM();
     
     /**
      * Range of data
      * @return
      */
     public int           range(); //for PWM and for A2D read, must be 1 for digital inputs.

     /**
      * Returns an I2CConnection object, which contains all the I2C information necessary for reading an I2C Device.
      * @return I2CConnection
      */
     public I2CConnection getI2CConnection(); //TODO: Grove Specific for non-I2C Devices
     
     /**
      * Checks if data coming from the sensor is valid
      * @param backing
      * @param position
      * @param length
      * @param mask
      * @return
      */
     public boolean		  isValid(byte[] backing, int position, int length, int mask);
     
     /**
      * Returns number of pins used by device.
      * @return
      */
     public int           pinsUsed();//count of contiguous pins used, eg almost always 1 but would be 2 for the grove rotary encoder
     
}
