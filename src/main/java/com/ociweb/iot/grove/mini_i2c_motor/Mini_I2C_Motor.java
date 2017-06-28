/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.mini_i2c_motor;

import static com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class Mini_I2C_Motor {
    FogCommandChannel target;
    
    public Mini_I2C_Motor(FogCommandChannel ch){
		this.target = ch;		
	}
    
    
    
    public void driveMotor1(int speed)
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
    
    public void driveMotor2(int speed)
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
    
    public void stopMotor1()
    {        

        writeSingleByteToRegister(target,CH1_ADD,CTL_REG,STOP);
        target.i2cFlushBatch();
        
    }
    
    public void stopMotor2()
    {        

        writeSingleByteToRegister(target,CH2_ADD,CTL_REG,STOP);
        target.i2cFlushBatch();
        
    }
    
    public void brakeMotor1()
    {        

        writeSingleByteToRegister(target,CH1_ADD,CTL_REG,BRAKE);
        target.i2cFlushBatch();
        
    }
    
    public void brakeMotor2()
    {        

        writeSingleByteToRegister(target,CH2_ADD,CTL_REG,BRAKE);
        target.i2cFlushBatch();
        
    }
    
    private boolean writeSingleByteToRegister(FogCommandChannel ch,int address, int register, int value) {
        if (!ch.i2cIsReady()) {
            return false;
        }

        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = ch.i2cCommandOpen(address);
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        ch.i2cCommandClose();
        return true;
    }    
}
