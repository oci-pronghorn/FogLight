/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.mini_motor_driver;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;

/**
 *
 * @author huydo
 */
public enum MiniMotorDriverTwig {
    ;
        public enum MiniMotorDriver implements I2CIODevice{
            
            checkFaultCH1(){
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {MiniMotorDriver_Constants.FAULT_REG};
                    byte I2C_ADDR = MiniMotorDriver_Constants.CH1_ADD;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = MiniMotorDriver_Constants.FAULT_REG;  //register identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            checkFaultCH2(){
                @Override
                public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
                    byte[] REG_ADDR = {MiniMotorDriver_Constants.FAULT_REG};
                    byte I2C_ADDR = MiniMotorDriver_Constants.CH2_ADD;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = MiniMotorDriver_Constants.FAULT_REG;  //register identifier
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
            public int defaultPullRateMS() {
                return 1000;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public MiniMotorDriver_Transducer newTransducer(FogCommandChannel...ch){
                return new MiniMotorDriver_Transducer(ch[0]);//TODO:feed the right chip enum, create two seperate twigs
            }
            
            
            /**
             * @return Delay, in milliseconds, for scan. TODO: What's scan?
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
             * @return false if the bytes returned from the device were not some valid response
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
            
        }
}

