/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

import static com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometer_Constants.OUT_X_L_A;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;

/**
 *
 * @author huydo
 */
public enum SixAxisAccelerometerTwig {
    ;
        public enum SixAxisAccelerometer implements I2CIODevice {
            // TODO: read temp
            readAccel(){
                
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {OUT_X_L_A};
                    byte I2C_ADDR = SixAxisAccelerometer_Constants.LSM303D_ADDR;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = SixAxisAccelerometer_Constants.OUT_X_L_A; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            readMag(){
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {SixAxisAccelerometer_Constants.OUT_X_L_M};
                    byte I2C_ADDR = SixAxisAccelerometer_Constants.LSM303D_ADDR;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = SixAxisAccelerometer_Constants.OUT_X_L_M; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            };
            @Override
            public boolean isInput() {
                return true;
            }
            
            @Override
            public boolean isOutput() {
                return true;
            }
            @Override
            public int response() {
                return 1000;
            }
            @SuppressWarnings("unchecked")
		@Override
		public SixAxisAccelerometer_Transducer newTransducer(FogCommandChannel... ch) {
			return new SixAxisAccelerometer_Transducer(ch[0], null, null, null);
		}
            /**
             *
             *
             * /**
             * @return Delay, in milliseconds, for scan. TODO: What's scan?
             */
            @Override
            public int scanDelay() {
                return 0;
            }
            
            /**
             * @return True if this twig is Pulse Width Modulated (PWM) device, and
             *         false otherwise.
             */
            @Override
            public boolean isPWM() {
                return false;
            }
            
            /**
             * @return True if this twig is an I2C device, and false otherwise.
             */
            public boolean isI2C() {
                return true;
            }
            
            
            
            /**
             * @return The possible value range for reads from this device (from zero).
             */
            @Override
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
             * @return false if the bytes returned from the device were not some valid response
             */
            @Override
            public boolean isValid(byte[] backing, int position, int length, int mask) {
                return true;
            }
            
            /**
             * @return The number of hardware pins that this twig uses.
             */
            @Override
            public int pinsUsed() {
                return 1;
            }
        }
}
