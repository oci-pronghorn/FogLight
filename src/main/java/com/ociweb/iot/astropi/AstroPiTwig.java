/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

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
                    return 1000;
                }
                @Override
                public I2CConnection getI2CConnection() {
                    byte[] REG_ADDR = {(byte)0b11110010};
                    byte[] SETUP = {};
                    byte I2C_ADDR = 0x46;
                    byte BYTESTOREAD = 1;
                    byte REG_ID = (byte)0b11110010; //just an identifier
                    return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, SETUP);
                }
            };
            
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
            public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        }
}
