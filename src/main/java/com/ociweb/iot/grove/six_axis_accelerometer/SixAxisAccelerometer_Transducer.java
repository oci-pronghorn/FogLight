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
    private final AccelValsListener accellistener;
    private final MagValsListener maglistener;
    private final TempValsListener tempListener;
    
    public SixAxisAccelerometer_Transducer(FogCommandChannel ch, AccelValsListener accellistener,  MagValsListener maglistener, TempValsListener tempListener) {
        this.target = ch;
        this.accellistener = accellistener;
        this.maglistener = maglistener;
        this.tempListener = tempListener;
        target.ensureI2CWriting(50, 4);
    }

    
    @Override 
    public void startup() {
        if (accellistener != null) {
            final int accelDataRate = accellistener.getAccerometerDataRate().getSpecification();
            final int accelAxes = accellistener.getAccerometerAxes();
            final int useRegisters = 0x08;
            axWriteByte(CTRL_REG1, accelDataRate | useRegisters | accelAxes);
            final int accelScale =  accellistener.getAccerometerScale().getSpecification();
            axWriteByte(CTRL_REG2, accelScale);
        }
        else {
            axWriteByte(CTRL_REG1, 0);
            axWriteByte(CTRL_REG2, 0);
        }

        axWriteByte(CTRL_REG3,0x00); //no interrupt
        axWriteByte(CTRL_REG4,0x00); //no interrupt

        if (maglistener != null) {
            final int magDataRate = maglistener.getMagneticDataRate().getSpecification();
            final int magScale = maglistener.getMagneticScale().getSpecification();
            final int magRes = maglistener.getMagneticRes().getSpecification();
            final int tempEnabled = tempListener != null ? 0x01<<8 : 0x00<<8;
            axWriteByte(CTRL_REG5, tempEnabled | magRes | magDataRate);
            axWriteByte(CTRL_REG6, magScale);
        }
        else {
            axWriteByte(CTRL_REG5, 0);
            axWriteByte(CTRL_REG6, 0);
        }

        axWriteByte(CTRL_REG7,0b10000000); // 0x00 = continouous conversion mode
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
            i2cPayloadWriter.writeByte(value & 0xFF);

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
