/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.adc;

import static com.ociweb.iot.grove.adc.ADC_Constants.ADDR_ADC121;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_ALERT;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_CONFIG;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_HYST;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_LIMITH;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_LIMITL;
import static com.ociweb.iot.grove.adc.ADC_Constants.REG_ADDR_RESULT;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.I2CListenerTransducer;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class ADC_Transducer implements IODeviceTransducer,I2CListenerTransducer{
    private final FogCommandChannel target;
    private AlertStatusListener alertListener;
    private ConversionResultListener resultListener;
    public ADC_Transducer(FogCommandChannel ch, ADCListener... l){
    	
        this.target = ch;
        target.ensureI2CWriting();
        for(ADCListener item:l){
            if(item instanceof AlertStatusListener){
                this.alertListener = (AlertStatusListener) item;
            }
            if(item instanceof ConversionResultListener){
                this.resultListener = (ConversionResultListener) item;
            }
        }
    }
    /**
     * Begin the ADC with the default configuration :
     * f_convert = 27 ksps; Alert Hold = 0;
     * Alert Flag Enable = 0; Alert Pin Enable = 0;
     * Polarity = 0.
     */
    public void begin(){
        writeSingleByteToRegister(REG_ADDR_CONFIG,REG_ADDR_CONFIGVal);
    }
    /**
     * Write a byte to the CONFIG_REG register
     * @param _b 
     */
    public void setCONFIG_REG(int _b){
        writeSingleByteToRegister(REG_ADDR_CONFIG,_b);
    }
    
    private int REG_ADDR_CONFIGVal = 0x20; // default configuration
    /**
     * Set the conversion rate of the device. 
     * 1 = 27 ksps, 2 = 13.5 ksps, 3 = 6.7 ksps
     * 4 = 3.4 ksps, 5 = 1.7 ksps, 6 = 0.9 ksps, 7 = 0.4 ksps
     * @param rate 
     */
    public void setRate(int rate){
        if(rate > 0){
            REG_ADDR_CONFIGVal |= (rate<<5); 
        }
    }
    /**
     * Set/ Clear the alert hold bit
     * @param alertHold true : set the bit; false: clear the bit 
     */
    public void setAlertHoldBit(boolean alertHold){
        if(alertHold){
            REG_ADDR_CONFIGVal |= (1<<4);
        }else{
            REG_ADDR_CONFIGVal &= 0b11101111;
        }
        writeSingleByteToRegister(REG_ADDR_CONFIG,REG_ADDR_CONFIGVal);
    }
    /**
     * Set/ Clear the alert flag enable bit
     * @param alertFlag true : set the bit; false: clear the bit 
     */
    public void setAlertFlagEnableBit(boolean alertFlag){
        if(alertFlag){
            REG_ADDR_CONFIGVal |= (1<<3);
        }else{
            REG_ADDR_CONFIGVal &= 0b11110111;
        }
        writeSingleByteToRegister(REG_ADDR_CONFIG,REG_ADDR_CONFIGVal);
    }
    
    /**
     * Set the lower limit threshold used to determine the alert condition
     * @param _b positive integer between 0 and 4095
     */
    public void setLowerLimit(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_LIMITL,value);
    }
    /**
     * Set the upper limit threshold used to determine the alert condition
     * @param _b positive integer between 0 and 4095
     */
    public void setUpperLimit(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_LIMITH,value);
    }
    /**
     * Set the hysteresis value
     * @param _b positive integer between 0 and 4095
     */
    public void setHysteresis(int _b){
        int[] value = {0,0};
        value[0] = (_b >>8) & 0xff;
        value[1] = _b & 0xff;
        writeTwoBytesToRegister(REG_ADDR_HYST,value);
    }
        /**
     * Convert the 2 bytes I2C read to the correct representation of the digital value
     * @param backing circular buffer containing data from I2C read
     * @param position index of the first byte
     * @param length length of the array
     * @param mask 
     * @return The converted digital value. 
     */
    public short interpretData(byte[] backing, int position, int length, int mask){
        //format the data from the circular buffer backing[]
        
        short temp = (short)(((backing[(position)&mask]&0x0F) << 8) | (backing[(position+1)&mask]&0xFF));
        
        return temp;
    }

    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    private void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADDR_ADC121);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    /**
     * write 2 bytes to a register
     * @param register 
     * @param value 
     */
    private void writeTwoBytesToRegister(int register, int[] value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADDR_ADC121);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value[0]);
        i2cPayloadWriter.writeByte(value[1]);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
}


    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == ADDR_ADC121){
            if(register == REG_ADDR_RESULT && null!=resultListener){
                resultListener.conversionResult(this.interpretData(backing, position, length, mask));
            }
            if(register == REG_ADDR_ALERT && null!=alertListener){
                int overRange = (backing[position] & 2)>>1;
                int underRange = (backing[position]& 1);
                alertListener.alertStatus(overRange, underRange);
            }
        }
    }

}
