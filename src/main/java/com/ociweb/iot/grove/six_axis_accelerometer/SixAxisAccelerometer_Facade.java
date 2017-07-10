/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

import static com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometer_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class SixAxisAccelerometer_Facade implements IODeviceFacade {
    FogCommandChannel target;
    
    public SixAxisAccelerometer_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    
    public void begin(){
        writeSingleByteToRegister(CTRL_REG1,0x57); // 0x57 = ODR=50hz, all accel axes on
        writeSingleByteToRegister(CTRL_REG2,(3<<6)|(0<<3)); // set full-scale
        writeSingleByteToRegister(CTRL_REG3,0x00); //no interrupt
        writeSingleByteToRegister(CTRL_REG4,0x00); //no interrupt
        writeSingleByteToRegister(CTRL_REG5,(4<<2)); //0x10 = mag 50 Hz output rate
        writeSingleByteToRegister(CTRL_REG6,MAG_SCALE_2); //magnetic scale = +/-1.3Gauss
        writeSingleByteToRegister(CTRL_REG7,0x00); // 0x00 = continouous conversion mode
    }
    /**
     * Convert the 6 bytes of X,Y,Z values to the correct two's complement representation
     * @param backing array containing 6 bytes
     * @param position index of the first byte
     * @param length length of the array
     * @param mask
     * @return array of 3 X,Y,Z values ,where array[0] = X, array[1] = Y
     */
    public short[] interpretXYZ(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]&0xFF) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]&0xFF) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]&0xFF) << 8) | (backing[(position+4)&mask]&0xFF));
        
        return temp;
    }
    
    public short[] interpretMag(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Mag data passed into the class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]&0xFF) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]&0xFF) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]&0xFF) << 8) | (backing[(position+4)&mask]&0xFF));
        
        return temp;
    }
    
    
    
    
    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(LSM303D_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
}
