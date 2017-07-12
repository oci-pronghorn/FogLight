/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.mini_motor_driver;

import static com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriver_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class MiniMotorDriver_Facade implements IODeviceFacade,I2CListener {
    private final FogCommandChannel target;
    private MiniMotorDriverListener listener;
    public MiniMotorDriver_Facade(FogCommandChannel ch){
        this.target = ch;
    }
    public MiniMotorDriver_Facade(FogCommandChannel ch,MiniMotorDriverListener l){
        this.target = ch;
        this.listener = l;
    }
    
    
    
    /**
     * Set power the motor on specified channel
     * @param channel which channel (1 or 2) to set the power to,
     * @param velocity velocity of the motor, from -63 to 63
     */
    public void setVelocity(int channel, int velocity)
    {
        // Before we do anything, we'll want to
        //  clear the fault status. To do that
        //  write 0x80 to register 0x01 on the
        //  DRV8830.
        int address = (channel==1)?CH1_ADD:CH2_ADD;
        
        writeSingleByteToRegister(address,FAULT_REG,CLEAR); // Clear the fault status.
        
        byte regValue = (byte)Math.abs(velocity);      // Find the byte-ish abs value of the input
        if (regValue > 63) {
            regValue = 63;
        } // Cap the value at 63.
        regValue = (byte) (regValue<<2);           // Left shift to make room for bits 1:0
        if (velocity < 0) {
            regValue |= 0x02;
        }  // Set bits 1:0 based on sign of input.
        else{
            regValue |= 0x01;
        }
        writeSingleByteToRegister(address,CTL_REG,regValue);
        
    }
    
    /**
     * Stop the motor on specified channel
     * @param channel name of channel (1 or 2)
     */
    public void stop(int channel)
    {
        int address = (channel==1)?CH1_ADD:CH2_ADD;
        writeSingleByteToRegister(address,CTL_REG,STOP);
        
    }
    
    /**
     * Brake the motor on specified channel
     * @param channel name of channel (1 or 2)
     */
    public void brake(int channel)
    {
        int address = (channel==1)?CH1_ADD:CH2_ADD;
        writeSingleByteToRegister(address,CTL_REG,BRAKE);
        
    }
    /**
     * write a byte to a register
     * @param address
     * @param register register to write to
     * @param value byte to write
     */
    public void writeSingleByteToRegister(int address, int register, int value) {
        
        
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == CH1_ADD){
            int status1 = (backing[position]>0)?1:0;
            listener.ch1FaultStatus(status1);
        }
        if(addr == CH2_ADD){
            int status2 = (backing[position]>0)?1:0;
            listener.ch2FaultStatus(status2);
        }
    }
}
