/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;


/**
 *
 * @author huydo
 */
public class Grove_Accelerometer implements IODevice {
    
    public static boolean isStarted = false;
    
    public static boolean begin(FogCommandChannel target) {
        if (!target.i2cIsReady()) {
            return false;
        }
        isStarted = true;

        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,0);
        target.i2cFlushBatch();
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,16);
        target.i2cFlushBatch();
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,8);
        target.i2cFlushBatch();
        return true;
    }
    
    private static void writeSingleByteToRegister(FogCommandChannel target, int address, int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);

        target.i2cCommandClose();
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
        return 0;
    }
    
    @Override
    public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
        byte[] ACC_READCMD = {Grove_Acc_Constants.ADXL345_DATAX0};
        byte[] ACC_SETUP = {Grove_Acc_Constants.ADXL345_POWER_CTL,8};
        byte ACC_ADDR = Grove_Acc_Constants.ADXL345_DEVICE;
        byte ACC_BYTESTOREAD = 6;
        byte ACC_REGISTER = Grove_Acc_Constants.ADXL345_DATAX0;
        return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
    }
    
    @Override
    public int response() {
        return 20;
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
    public int scanDelay() {
        return 0;
    }
}
