/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.accelerometer;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import static com.ociweb.iot.grove.accelerometer.Accelerometer_16G_Constants.*;
import com.ociweb.iot.maker.IODeviceFacade;

/**
 *
 * @author huydo
 */
public class Accelerometer_16G_Facade implements IODeviceFacade {
    FogCommandChannel target;
    public static final int ADXL345_DEVICE    = 0x53;

    public Accelerometer_16G_Facade(FogCommandChannel ch){
        this.target = ch;
    }
         
    public void begin() {
        
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,0);
              
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,16);
                
        writeSingleByteToRegister(Accelerometer_16G_Constants.ADXL345_POWER_CTL,8);
                        
    }
    
    public void setRange(int range){
        byte _s;
        switch(range){
            case 2:
                _s = 0b00001000;
                break;
            case 4:
                _s = 0b00001001;
                break;
            case 8:
                _s = 0b00001010;
                break;
            case 16:
                _s = 0b00001011;
                break;
            default:
                _s = 0b00001011;
        }
        writeSingleByteToRegister(ADXL345_DATA_FORMAT,_s);
                
    }
    
    // Sets the OFSX, OFSY and OFSZ bytes
// OFSX, OFSY and OFSZ are user offset adjustments in twos complement format with
// a scale factor of 15,6mg/LSB
// OFSX, OFSY and OFSZ should be comprised between
    public void setAxisOffset(int x, int y, int z) {
        
        writeSingleByteToRegister(ADXL345_OFSX,(byte) x);
        
        writeSingleByteToRegister(ADXL345_OFSY,(byte) y);
        
        writeSingleByteToRegister(ADXL345_OFSZ,(byte) z);
    }
    
    public void setRate(int rate){
        byte _s;
        switch(rate){
            case 3200:
                _s = ADXL345_RATE_3200;
                break;
            case 1600:
                _s = ADXL345_RATE_1600;
                break;
            case 800:
                _s = ADXL345_RATE_800;
                break;
            case 400:
                _s = ADXL345_RATE_400;
                break;
            case 200:
                _s = ADXL345_RATE_200;
                break;
            case 100:
                _s = ADXL345_RATE_100;
                break;
            case 50:
                _s = ADXL345_RATE_50;
                break;
            case 25:
                _s = ADXL345_RATE_25;
                break;
            case 12:
                _s = ADXL345_RATE_12;
                break;
            case 6:
                _s = ADXL345_RATE_6;
                break;
            default:
                _s = ADXL345_RATE_400;
        }
        writeSingleByteToRegister(ADXL345_DATA_FORMAT,_s);
        
        
    }
    
    
    public short[] intepretData(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the NunchuckTwig class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]) << 8) | (backing[(position+4)&mask]&0xFF));
        
        return temp;
    }
    
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(ADXL345_DEVICE);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
}
