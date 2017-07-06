/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.i2c_adc;

import static com.ociweb.iot.grove.i2c_adc.I2C_ADC_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class I2C_ADC_Facade implements IODeviceFacade{
    FogCommandChannel target;
    
    public I2C_ADC_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    public void begin(){
        writeSingleByteToRegister(REG_ADDR_CONFIG,0x20);
    }
    
    public void setCONFIG_REG(int _b){
        writeSingleByteToRegister(REG_ADDR_CONFIG,_b);
    }
    
    public short intepretData(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        short temp = (short)(((backing[(position)&mask]&0x0F) << 8) | (backing[(position+1)&mask]&0xFF));
        
        return temp;
    }
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADDR_ADC121);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
}