/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

import static com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometer_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;


/**
 *
 * @author huydo
 */
public class SixAxisAccelerometer_Facade implements IODeviceFacade,I2CListener {
    private final FogCommandChannel target;
    private AccelValsListener accellistener;
    private MagValsListener maglistener;
    public SixAxisAccelerometer_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    public SixAxisAccelerometer_Facade(FogCommandChannel ch, SixAxisAccelerometer_16gListener... l ){
        this.target = ch;
        for(SixAxisAccelerometer_16gListener item:l){
            if(item instanceof AccelValsListener){
                this.accellistener = (AccelValsListener) item;
            }
            if(item instanceof MagValsListener){
                this.maglistener = (MagValsListener) item;
            }
        }
    }
    /**
     * Start the accelerometer sensor with the following configurations:
     * 50Hz accelerometer data rate, all acceleration axis enabled, normal mode
     * acceleration full scale with +/- 2g
     * no interrupts
     * enable temperature
     * 50Hz magnetic data rate; +/- 2 gauss, continuous conversion mode
     */
    public void begin(){
        writeSingleByteToRegister(CTRL_REG1,0x57); // 0x57 = ODR=50hz, all accel axes on
        writeSingleByteToRegister(CTRL_REG2,0b11000000); // set full-scale
        writeSingleByteToRegister(CTRL_REG3,0x00); //no interrupt
        writeSingleByteToRegister(CTRL_REG4,0x00); //no interrupt
        writeSingleByteToRegister(CTRL_REG5,0b10010000); //0x10 = magnetic 50 Hz output rate, enable temperature
        writeSingleByteToRegister(CTRL_REG6,MAG_SCALE_2); //magnetic scale = +/- 2 Gauss
        writeSingleByteToRegister(CTRL_REG7,0x80); 
    }
    /**
     * write a byte to CTRL_REG1
     * @param _b 
     */
    public void setCTRL_REG1(int _b){
        writeSingleByteToRegister(CTRL_REG1,_b);
    }
    /**
     * write a byte to CTRL_REG2
     * @param _b 
     */
    public void setCTRL_REG2(int _b){
        writeSingleByteToRegister(CTRL_REG2,_b);
    }
    /**
     * write a byte to CTRL_REG3
     * @param _b 
     */
    public void setCTRL_REG3(int _b){
        writeSingleByteToRegister(CTRL_REG3,_b);
    }
    /**
     * write a byte to CTRL_REG4
     * @param _b 
     */
    public void setCTRL_REG4(int _b){
        writeSingleByteToRegister(CTRL_REG4,_b);
    }
    /**
     * write a byte to CTRL_REG5
     * @param _b 
     */
    public void setCTRL_REG5(int _b){
        writeSingleByteToRegister(CTRL_REG5,_b);
    }
    /**
     * write a byte to CTRL_REG6
     * @param _b 
     */
    public void setCTRL_REG6(int _b){
        writeSingleByteToRegister(CTRL_REG6,_b);
    }
    /**
     * write a byte to CTRL_REG7
     * @param _b 
     */
    public void setCTRL_REG7(int _b){
        writeSingleByteToRegister(CTRL_REG7,_b);
    }
    
    /**
     * Convert the 6 bytes of X,Y,Z values to the correct two's complement representation
     * @param backing array containing 6 bytes
     * @param position index of the first byte
     * @param length length of the array
     * @param mask
     * @return array of 3 X,Y,Z values ,where array[0] = X, array[1] = Y
     */
    public short[] interpretData(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the class";
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

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == LSM303D_ADDR){
            if(register == OUT_X_L_A){
                short[] xyz_accel = this.interpretData(backing, position, length, mask);
                accellistener.accelVals(xyz_accel[0], xyz_accel[1], xyz_accel[2]);
            }
            if(register == OUT_X_L_M){
                short[] xyz_mag = this.interpretData(backing, position, length, mask);
                maglistener.magVals(xyz_mag[0], xyz_mag[1], xyz_mag[2]);
            }
        }

    }

}
