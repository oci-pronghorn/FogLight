/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

import static com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometer_Constants.*;

import com.ociweb.gl.api.transducer.StartupListenerTransducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.I2CListenerTransducer;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;


/**
 *
 * @author huydo
 */
public class SixAxisAccelerometer_Transducer implements IODeviceTransducer,I2CListenerTransducer,StartupListenerTransducer {
    private final FogCommandChannel target;
    private AccelValsListener accellistener;
    private MagValsListener maglistener;

    // TODO: why are mutations on these accumulated?
    private int CTRL_REG2Val = 0b00000000; //by default, acceleration full scale = +/- 2g
    private int CTRL_REG1Val = 0b01011111; //by default, ODR = 50 Hz, enable BDU, all accel axes enabled
    private int CTRL_REG5Val = 0b10010000; //by default, enable temperature sensor, magnetic data low resolution, ODR = 50 Hz
    private int CTRL_REG6Val = 0x00;
    
    public SixAxisAccelerometer_Transducer(FogCommandChannel ch) {
        this.target = ch;
        target.ensureI2CWriting(50, 4);
    }

    public void registerListeners(AccelValsListener accellistener,  MagValsListener maglistener) {
        this.accellistener = accellistener;
        this.maglistener = maglistener;
    }

    /**
     * Start the accelerometer sensor with the following configurations:
     * 50Hz accelerometer data rate, all acceleration axis enabled, normal mode
     * acceleration full scale with +/- 2g
     * no interrupts
     * enable temperature
     * 50Hz magnetic data rate; +/- 2 gauss, continuous conversion mode
     */
    
    @Override 
    public void startup() {
        axWriteByte(CTRL_REG1,CTRL_REG1Val);
        axWriteByte(CTRL_REG2,CTRL_REG2Val); // set full-scale
        axWriteByte(CTRL_REG3,0x00); //no interrupt
        axWriteByte(CTRL_REG4,0x00); //no interrupt
        axWriteByte(CTRL_REG5,CTRL_REG5Val); //0x10 = magnetic 50 Hz output rate, enable temperature
        axWriteByte(CTRL_REG6,CTRL_REG6Val); //magnetic scale = +/- 2 Gauss
        axWriteByte(CTRL_REG7,0b10000000);
    }

    /**
     * Set accelerometer output data rate 
     * @param aRate 
     * 1 = 3Hz, 2 = 6Hz, 3 =12 Hz,  4 = 25 Hz, 5 = 50Hz
     * 6 = 100 Hz, 7 = 200 Hz, 8 = 400 Hz
     * 9 = 800 Hz, 10 = 1600 Hz
     */
    // TODO: make enum
    public boolean setAccelODR(int aRate) {
        if(aRate != 0){
            CTRL_REG1Val |= (aRate<<4);
        }
        return axWriteByte(CTRL_REG1,CTRL_REG1Val);
    }

    /**
     * Set the full scale of acceleration data
     * @param aScale 2,4,6,8 or 16 (gauss)
     * 
     */
    // TODO: make enum
    public boolean setAccelScale(int aScale) {
        switch(aScale){
            case 2:
                CTRL_REG2Val |= (0<<3);
                break;
            case 4:
                CTRL_REG2Val |= (1<<3);
                break;
            case 6:
                CTRL_REG2Val |= (2<<3);
                break;
            case 8:
                CTRL_REG2Val |= (3<<3);
                break;
            case 16:
                CTRL_REG2Val |= (4<<3);
                break;
        }
        return axWriteByte(CTRL_REG2,CTRL_REG2Val);
    }

    /**
     * Set the output data rate of the magnetometer
     * @param mRate 
     * 0 = 3Hz, 1 = 6 Hz, 2 = 12 Hz
     * 3 = 25 Hz, 4 = 50 Hz
     */
    // TODO: make enum
    public boolean setMagODR(int mRate) {
        CTRL_REG5Val |= (mRate << 2);
        return axWriteByte(CTRL_REG5,CTRL_REG5Val);
    }

    /**
     * Set the full scale of magnetometer data
     * @param mScale 2,4,8 or 12 (gauss)
     */
    // TODO: make enum
    public boolean setMagScale(int mScale) {
        switch(mScale){
            case 2:
                CTRL_REG6Val |= (0<<5);
                break;
            case 4:
                CTRL_REG6Val |= (1<<5);
                break;
            case 8:
                CTRL_REG6Val |= (2<<5);
                break;
            case 12:
                CTRL_REG6Val |= (3<<5);
                break;

        }
        return axWriteByte(CTRL_REG6,CTRL_REG6Val);
    }

    /**
     * write a byte to a register
     * @param register register to write to
     * @param value byte to write
     */
    private boolean axWriteByte(int register, int value) {
        if (target.i2cIsReady(1)) {
            DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(LSM303D_ADDR);

            i2cPayloadWriter.writeByte(register);
            i2cPayloadWriter.writeByte(value);

            target.i2cCommandClose(i2cPayloadWriter);
            target.i2cFlushBatch();
            return true;
        }
        return false;
    }

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if (addr == LSM303D_ADDR) {
            if (accellistener != null && register == OUT_X_L_A) {
                short[] xyz_accel = this.interpretData(backing, position, length, mask);
                accellistener.accelerationValues(xyz_accel[0], xyz_accel[1], xyz_accel[2]);
            }
            if (maglistener != null && register == OUT_X_L_M) {
                short[] xyz_mag = this.interpretData(backing, position, length, mask);
                maglistener.magneticValues(xyz_mag[0], xyz_mag[1], xyz_mag[2]);
            }
        }
    }

    /**
     * Convert the 6 bytes of X,Y,Z values to the correct two's complement representation
     * @param backing array containing 6 bytes
     * @param position index of the first byte
     * @param length length of the array
     * @param mask
     * @return array of 3 X,Y,Z values ,where array[0] = X, array[1] = Y
     */
    private short[] temp = {0,0,0};
    private short[] interpretData(byte[] backing, int position, int length, int mask) {
        assert(length==6) : "Non-Accelerometer data passed into the class";
        //format the data from the circular buffer backing[]
        temp[0] = (short)(((backing[(position+1)&mask]&0xFF) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]&0xFF) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]&0xFF) << 8) | (backing[(position+4)&mask]&0xFF));
        return temp;
    }
}
