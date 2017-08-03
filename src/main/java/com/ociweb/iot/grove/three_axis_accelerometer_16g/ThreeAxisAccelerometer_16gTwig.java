/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.three_axis_accelerometer_16g;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;

/**
 *
 * @author huydo
 */
public enum ThreeAxisAccelerometer_16gTwig {
    ;
        public enum ThreeAxisAccelerometer_16g implements I2CIODevice {
            
            GetXYZ(){
                
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {ThreeAxisAccelerometer_16g_Constants.ADXL345_DATAX0};
                    byte I2C_ADDR = ThreeAxisAccelerometer_16g_Constants.ADXL345_DEVICE;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = ThreeAxisAccelerometer_16g_Constants.ADXL345_DATAX0; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            GetTapAct(){
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {ThreeAxisAccelerometer_16g_Constants.ADXL345_ACT_TAP_STATUS};
                    byte I2C_ADDR = ThreeAxisAccelerometer_16g_Constants.ADXL345_DEVICE;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = ThreeAxisAccelerometer_16g_Constants.ADXL345_ACT_TAP_STATUS; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            GetInterrupt(){
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {ThreeAxisAccelerometer_16g_Constants.ADXL345_INT_SOURCE};
                    byte I2C_ADDR = ThreeAxisAccelerometer_16g_Constants.ADXL345_DEVICE;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = ThreeAxisAccelerometer_16g_Constants.ADXL345_INT_SOURCE; //just an identifier
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
            
            @Override
            public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
