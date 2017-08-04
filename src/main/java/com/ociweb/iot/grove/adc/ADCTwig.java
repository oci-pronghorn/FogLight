/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.adc;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;

/**
 *
 * @author huydo
 */
public enum ADCTwig {
    ;
    public enum ADC implements I2CIODevice {
    ReadConversionResult(){
        @Override
        public I2CConnection getI2CConnection() {
            byte[] REG_ADDR = {ADC_Constants.REG_ADDR_RESULT};
            byte I2C_ADDR = ADC_Constants.ADDR_ADC121;
            byte BYTESTOREAD = 2;
            byte REG_ID = ADC_Constants.REG_ADDR_RESULT; //just an identifier
            return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
        }
    },
    ReadAlertStatus(){
      @Override
        public I2CConnection getI2CConnection() {
            byte[] REG_ADDR = {ADC_Constants.REG_ADDR_ALERT};
            byte I2C_ADDR = ADC_Constants.ADDR_ADC121;
            byte BYTESTOREAD = 1;
            byte REG_ID = ADC_Constants.REG_ADDR_ALERT; //just an identifier
            return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, null);
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
            return false;
        }
        
        @Override
        public boolean isPWM() {
            return false;
        }
        
        @Override
        public int range() {
            return 256;
        }
        @Override
        public boolean isValid(byte[] backing, int position, int length, int mask) {
            return true;
        }
        
        @Override
        public int pinsUsed() {
            return 1;
        }
        @Override
        public <F extends IODeviceTransducer> F newTransducer(FogCommandChannel... ch) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        

}
    };

