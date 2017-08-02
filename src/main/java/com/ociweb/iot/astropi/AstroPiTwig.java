/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;

/**
 *
 * @author huydo
 */
public enum AstroPiTwig {
    ;
        public enum AstroPi implements I2CIODevice{
            GetJoystick(){
                
                @Override
                public int response() {
                    return 400;
                }
                @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.JOYSTICK_REG_ADDR};
                    byte I2C_ADDR = AstroPi_Constants.LED_I2C_ADDR;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = AstroPi_Constants.JOYSTICK_REG_ADDR; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            GetGyro(){
                @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.OUT_X_L_G};
                    byte I2C_ADDR = AstroPi_Constants.LSM9DS1_AG_ADDR;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = AstroPi_Constants.OUT_X_L_G; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            GetAccel(){
                @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.OUT_X_L_XL};
                    byte I2C_ADDR = AstroPi_Constants.LSM9DS1_AG_ADDR;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = AstroPi_Constants.OUT_X_L_XL; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
            GetMag(){
                @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.OUT_X_L_M};
                    byte I2C_ADDR = AstroPi_Constants.LSM9DS1_M_ADDR;
                    byte BYTESTOREAD = 6;
                    byte REG_ID = AstroPi_Constants.OUT_X_L_M; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }
            },
//            GetHumidity(){;
//            @Override
//                public I2CConnection getI2CConnection() {
//                    byte[] REG_ADDR = {AstroPi_Constants.HUMIDITY_L_REG};
//                    byte I2C_ADDR = AstroPi_Constants.HTS221_ADDRESS;
//                    byte BYTESTOREAD = 2;
//                    byte REG_ID = AstroPi_Constants.HUMIDITY_L_REG; //just an identifier
//                    byte[] SETUP = {AstroPi_Constants.CALIB_START};
//                    int bytesToReadAtSetUp = 16;
//                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, bytesToReadAtSetUp,SETUP);
//                }        
//            },
            GetHumidity1(){;
            @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.HUMIDITY_L_REG};
                    byte I2C_ADDR = AstroPi_Constants.HTS221_ADDRESS;
                    byte BYTESTOREAD = 2;
                    byte REG_ID = AstroPi_Constants.HUMIDITY_L_REG; //just an identifier

                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }        
            },
            GetTempFromHumiditySensor(){;
            @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.TEMP_L_REG_HUM};
                    byte I2C_ADDR = AstroPi_Constants.HTS221_ADDRESS;
                    byte BYTESTOREAD = 2;
                    byte REG_ID = AstroPi_Constants.TEMP_L_REG_HUM; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }        
            },
            GetTempFromPressureSensor(){
            @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.TEMP_L_REG_P};
                    byte I2C_ADDR = AstroPi_Constants.LPS25H_ADDRESS;
                    byte BYTESTOREAD = 2;
                    byte REG_ID = AstroPi_Constants.TEMP_L_REG_P; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }      
            },
            GetPressure(){
            @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.PRESSURE_XL_REG};
                    byte I2C_ADDR = AstroPi_Constants.LPS25H_ADDRESS;
                    byte BYTESTOREAD = 3;
                    byte REG_ID = AstroPi_Constants.PRESSURE_XL_REG; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
                }    
            },
            CalibrateHumiditySensor(){
            @Override
            public int response(){
                return 10000;
            }    
            @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {AstroPi_Constants.CALIB_START};
                    byte[] SETUP = {};
                    byte I2C_ADDR = AstroPi_Constants.HTS221_ADDRESS;
                    byte BYTESTOREAD = 16;
                    byte REG_ID = AstroPi_Constants.CALIB_START; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, SETUP);
                }    
            };
            @Override
                public int response() {
                    return 1000;
                }
            @Override
            public int scanDelay() {
                return 0;
            }
            
            @Override
            public boolean isInput() {
                return true;
            }
            
            @Override
            public boolean isOutput() {
                return true;
            }
            
            @Override
            public boolean isPWM() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
            @Override
            public int range() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
            @Override
            public I2CConnection getI2CConnection() {
                return null;
            }
            
            @Override
            public boolean isValid(byte[] backing, int position, int length, int mask) {
                return true;
            }
            
            @Override
            public int pinsUsed() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
            @Override
            public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        }
}
