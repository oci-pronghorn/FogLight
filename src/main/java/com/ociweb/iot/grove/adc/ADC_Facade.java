/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.adc;

import static com.ociweb.iot.grove.adc.ADC_Constants.*;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class ADC_Facade implements IODeviceFacade{
    FogCommandChannel target;
    
    public ADC_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    /**
     * Begin the ADC with the default configuration :
     * f_convert = 27 ksps; Alert Hold = 0;
     * Alert Flag Enable = 0; Alert Pin Enable = 0;
     * Polarity = 0.
     */
    public void begin(){
        writeSingleByteToRegister(REG_ADDR_CONFIG,0x20);
    }
    /**
     * Write a byte to the CONFIG_REG register
     * @param _b 
     */
    public void setCONFIG_REG(int _b){
        writeSingleByteToRegister(REG_ADDR_CONFIG,_b);
    }
    /**
     * Convert the 2 bytes I2C read to the correct representation of the digital value
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length length of the array
     * @param mask 
     * @return The converted digital value. 
     */
    
    public void setLowerLimit(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_LIMITL,value);
    }
    
    public void setUpperLimit(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_LIMITH,value);
    }
    
    public void setHysteresis(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_HYST,value);
    }
    
    public short interpretData(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        short temp = (short)(((backing[(position)&mask]&0x0F) << 8) | (backing[(position+1)&mask]&0xFF));
        
        return temp;
    }
    public int readAlertFlag(byte[] backing, int position, int length, int mask){
        
        return ((backing[(position)&mask]) >>> 7);
    }
    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADDR_ADC121);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    public void writeTwoBytesToRegister(int register, int[] value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADDR_ADC121);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value[0]);
        i2cPayloadWriter.writeByte(value[1]);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
}
}