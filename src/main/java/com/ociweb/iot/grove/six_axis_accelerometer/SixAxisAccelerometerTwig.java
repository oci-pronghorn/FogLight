/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

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
        readAccel() {
            @Override
            public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                byte[] REG_ADDR = {SixAxisAccelerometer_Transducer.OUT_X_L_A};
                byte I2C_ADDR = SixAxisAccelerometer_Transducer.LSM303D_ADDR;
                byte BYTESTOREAD = 6;
                byte REG_ID = SixAxisAccelerometer_Transducer.OUT_X_L_A; //just an identifier
                return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
            }
        },

        readMag() {
            @Override
            public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                byte[] REG_ADDR = {SixAxisAccelerometer_Transducer.OUT_X_L_M};
                byte I2C_ADDR = SixAxisAccelerometer_Transducer.LSM303D_ADDR;
                byte BYTESTOREAD = 6;
                byte REG_ID = SixAxisAccelerometer_Transducer.OUT_X_L_M; //just an identifier
                return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
            }
        },

        // TODO: read temp
        readTemp() {
            public I2CConnection getI2CConnection() {
                return null;
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
        public int defaultPullRateMS() {
            return 1000;
        }

        @Override
        public int pullResponseMinWaitNS() {
            return 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SixAxisAccelerometer_Transducer newTransducer(FogCommandChannel... ch) {
            return new SixAxisAccelerometer_Transducer(ch[0], null, null, null);
        }

        @Override
        public boolean isPWM() {
            return false;
        }

        /**
         * @return The possible value range for reads from this device (from zero).
         */
        @Override
        public int range() {
            return 256;
        }

        /**
         * Validates if the I2C data from from the device is a valid response for this twig
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
