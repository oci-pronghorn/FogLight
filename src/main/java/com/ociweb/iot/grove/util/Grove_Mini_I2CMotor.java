/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.util;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

import static com.ociweb.iot.grove.util.Grove_Mini_I2CMotor_Constants.*;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;

/**
 *
 * @author huydo
 */
public class Grove_Mini_I2CMotor implements IODevice {
    
    public static void driveMotor1(FogCommandChannel target, int speed)
    {        
        // Before we do anything, we'll want to
        //  clear the fault status. To do that
        //  write 0x80 to register 0x01 on the
        //  DRV8830.
        
        writeSingleByteToRegister(target,CH1_ADD,FAULT_REG,CLEAR); // Clear the fault status.
        
        byte regValue = (byte)Math.abs(speed);      // Find the byte-ish abs value of the input
        if (regValue > 63) {
            regValue = 63;
        } // Cap the value at 63.
        regValue = (byte) (regValue<<2);           // Left shift to make room for bits 1:0
        if (speed < 0) {
            regValue |= 0x02;
        }  // Set bits 1:0 based on sign of input.
        else{
            regValue |= 0x01;
        }
        writeSingleByteToRegister(target,CH1_ADD,CTL_REG,regValue);
        target.i2cFlushBatch();
        
    }
    
    public static void driveMotor2(FogCommandChannel target, int speed)
    {        
        // Before we do anything, we'll want to
        //  clear the fault status. To do that
        //  write 0x80 to register 0x01 on the
        //  DRV8830.
        
        writeSingleByteToRegister(target,CH2_ADD,FAULT_REG,CLEAR); // Clear the fault status.
        
        byte regValue = (byte)Math.abs(speed);      // Find the byte-ish abs value of the input
        if (regValue > 63) {
            regValue = 63;
        } // Cap the value at 63.
        regValue = (byte) (regValue<<2);           // Left shift to make room for bits 1:0
        if (speed < 0) {
            regValue |= 0x02;
        }  // Set bits 1:0 based on sign of input.
        else{
            regValue |= 0x01;
        }        
        writeSingleByteToRegister(target,CH2_ADD,CTL_REG,regValue);
        target.i2cFlushBatch();
        
    }
    
    public static void stopMotor1(FogCommandChannel target)
    {        

        writeSingleByteToRegister(target,CH1_ADD,CTL_REG,0);
        target.i2cFlushBatch();
        
    }
    
    public static void stopMotor2(FogCommandChannel target)
    {        

        writeSingleByteToRegister(target,CH2_ADD,CTL_REG,0);
        target.i2cFlushBatch();
        
    }
    
    public static void brakeMotor1(FogCommandChannel target)
    {        

        writeSingleByteToRegister(target,CH1_ADD,CTL_REG,0x03);
        target.i2cFlushBatch();
        
    }
    
    public static void brakeMotor2(FogCommandChannel target)
    {        

        writeSingleByteToRegister(target,CH2_ADD,CTL_REG,0x03);
        target.i2cFlushBatch();
        
    }
    
    private static boolean writeSingleByteToRegister(FogCommandChannel target, int address, int register, int value) {
        if (!target.i2cIsReady()) {
            return false;
        }

        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        return true;
    }
    
    @Override
    public boolean isInput() {
        return true;
    }
    
    @Override
    public boolean isOutput() {
        return true;
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
        byte[] MOTOR_READCMD = {FAULT_REG};
        byte[] MOTOR_SETUP = {FAULT_REG,CLEAR};
        byte MOTOR_ADDR = CH1_ADD;
        byte MOTOR_BYTESTOREAD = 1;
        byte MOTOR_REGISTER = FAULT_REG;
        return new I2CConnection(this, MOTOR_ADDR, MOTOR_READCMD, MOTOR_BYTESTOREAD, MOTOR_REGISTER, MOTOR_SETUP);
    }
    
    @Override
    public int response() {
        return 1000;
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

