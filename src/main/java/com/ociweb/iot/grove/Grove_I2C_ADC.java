/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove;

import com.ociweb.iot.grove.I2C_ADC.I2C_ADC_Constants;
import com.ociweb.iot.grove.I2C_ADC.I2C_ADC_Facade;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;

/**
 *
 * @author huydo
 */
public enum Grove_I2C_ADC implements I2CIODevice{
    ReadConversionResult(){
        @Override
        public I2CConnection getI2CConnection() {
            byte[] ACC_READCMD = {I2C_ADC_Constants.REG_ADDR_RESULT};
            byte[] ACC_SETUP = {};
            byte ACC_ADDR = I2C_ADC_Constants.ADDR_ADC121;
            byte ACC_BYTESTOREAD = 2;
            byte ACC_REGISTER = I2C_ADC_Constants.REG_ADDR_RESULT; //just an identifier
            return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
        }
    },
    ReadAlertStatus(){
      @Override
        public I2CConnection getI2CConnection() {
            byte[] ACC_READCMD = {I2C_ADC_Constants.REG_ADDR_ALERT};
            byte[] ACC_SETUP = {};
            byte ACC_ADDR = I2C_ADC_Constants.ADDR_ADC121;
            byte ACC_BYTESTOREAD = 1;
            byte ACC_REGISTER = I2C_ADC_Constants.REG_ADDR_ALERT; //just an identifier
            return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
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
        
         @SuppressWarnings("unchecked")
        @Override
        public I2C_ADC_Facade newFacade(FogCommandChannel...ch){
            return new I2C_ADC_Facade(ch[0]);//TODO:feed the right chip enum, create two seperate twigs
        }
    };

