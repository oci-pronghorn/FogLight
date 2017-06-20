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
public class Grove_Accelerometer {
    
    public static boolean isStarted = false;
    
    public static boolean begin(FogCommandChannel target) {
        if (!target.i2cIsReady()) {
            return false;
        }
        isStarted = true;

        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,0);

        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,16);

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
    
    private static void readFrom(FogCommandChannel target, int address, int register, byte[] _buff) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);
        i2cPayloadWriter.writeByte(register);
        target.i2cCommandClose();

//        DataInputBlobReader<I2CCommandSchema> i2cPayloadReader = target.i2cCommandOpen(address);
//        i2cPayloadReader.read(_buff, 0, 6);
//        target.i2cCommandClose();
        
    }
    private static void readXYZ(FogCommandChannel target, double x,double y,double z){
        byte[] _buff = new byte[6];
        readFrom(target,Grove_Acc_Constants.ADXL345_DEVICE,Grove_Acc_Constants.ADXL345_DATAX0,_buff);
        x = (short)(((( short)_buff[1]) << 8) | _buff[0])*Grove_Acc_Constants.X_GAIN;   
        y = (short)(((( short)_buff[3]) << 8) | _buff[2])*Grove_Acc_Constants.Y_GAIN;
        z = (short)(((( short)_buff[5]) << 8) | _buff[4])*Grove_Acc_Constants.Z_GAIN;
        
        
    }
    
    
    
}
