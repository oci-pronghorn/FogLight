/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.motor_driver;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.motor_driver.MotorDriver_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class MotorDriver_Transducer implements IODeviceTransducer,StartupListener{
    private final FogCommandChannel target;
    public int DRIVER_I2C_ADD = 0x0f; //default address of the driver

    public MotorDriver_Transducer(FogCommandChannel ch){
        this.target = ch;
        target.ensureI2CWriting(3000, 5);
    }
    
    public MotorDriver_Transducer(FogCommandChannel ch,int i2cAddress){
        this.target = ch;
        target.ensureI2CWriting(3000, 5);
        this.DRIVER_I2C_ADD = i2cAddress;
    }
        
    @Override
    public void startup() { //set registers on the driver to 0
        direction(0x00);
        setPower(0,0);
    }

    public int getMinVelocity() {
        return -255;
    }

    public int getMaxVelocity() {
        return 255;
    }

    /**
     * Set the velocity of the motor on both channel
     * @param channel1Power integer between -255 and 255
     * @param channel2Power integer between -255 and 255
     */
    public boolean setPower(int channel1Power,int channel2Power){

        int direction = M1CW_M2CW;
        if(channel1Power >= 0 && channel2Power >= 0){
            direction = M1CW_M2CW;
        }else if(channel1Power < 0 && channel2Power<0){
            direction = M1ACW_M2ACW;
        }else if(channel1Power >= 0 && channel2Power < 0){
            direction = M1CW_M2ACW;
        }else if(channel1Power < 0 && channel2Power >= 0){
            direction = M1ACW_M2CW;
        }

        int actualchannel1Power = Math.abs(channel1Power);
        int actualchannel2Power = Math.abs(channel2Power);
        if (actualchannel1Power > 255) actualchannel1Power = 255;
        if (actualchannel2Power > 255) actualchannel2Power = 255;

        if (target.i2cIsReady(2)) {
            {
                DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DRIVER_I2C_ADD);
                i2cPayloadWriter.writeByte(DIR_REG);
                i2cPayloadWriter.writeByte(direction);
                i2cPayloadWriter.writeByte(DUMMY_BYTE);
                target.i2cCommandClose(i2cPayloadWriter);
            }
            {
                DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DRIVER_I2C_ADD);
                i2cPayloadWriter.writeByte(SPEED_REG);
                i2cPayloadWriter.writeByte(actualchannel1Power);
                i2cPayloadWriter.writeByte(actualchannel2Power);
                target.i2cCommandClose(i2cPayloadWriter);
            }
            target.i2cDelay(DRIVER_I2C_ADD, 4000000);
            target.i2cFlushBatch();
            return true;
        }
        return false;
    }
    
    public void setFrequency(int frequency){
        byte _s;
        switch(frequency){
            case 31372:
                _s = F_31372Hz;
                break;
            case 3921:
                _s = F_3921Hz;
                break;
            case 490:
                _s = F_490Hz;
                break;
            case 122:
                _s = F_122Hz;
                break;
            case 30:
                _s = F_30Hz;
            default:
                _s = F_3921Hz;
        }
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DRIVER_I2C_ADD);
        
        i2cPayloadWriter.writeByte(PWM_FREQ_REG);
        i2cPayloadWriter.writeByte(_s);
        i2cPayloadWriter.writeByte(DUMMY_BYTE);
        
        target.i2cCommandClose(i2cPayloadWriter);
        target.i2cFlushBatch();
        target.i2cDelay(DRIVER_I2C_ADD, 4000000);
    }
    
    /**
     *      Drive a stepper motor
     * _step: -1024~1024, when _step>0, stepper motor runs clockwise; when _step is less than 0,
     * stepper motor runs anticlockwise
     * @param _step
     */
    // TODO: better job of i2c command usage
    public void StepperRun(int _step) {
        int _direction = 1;
        if (_step > 0) {
            _direction = 1; //clockwise
            _step = _step > 1024 ? 1024 : _step;
        }
        else if (_step < 0) {
            _direction = -1; //anti-clockwise
            _step = _step < -1024 ? 1024 : -(_step);
        }
        
        
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DRIVER_I2C_ADD);
        
        i2cPayloadWriter.writeByte(SPEED_REG);
        i2cPayloadWriter.writeByte(255);
        i2cPayloadWriter.writeByte(255);
        
        target.i2cCommandClose(i2cPayloadWriter);
        target.i2cFlushBatch();
        target.i2cDelay(DRIVER_I2C_ADD, 4000000);				                // wait
        
        if (_direction == 1) {
            for (int i=0; i<_step; i++) {
                direction(0b0001);
                direction(0b0011);
                direction(0b0010);
                direction(0b0110);
                direction(0b0100);
                direction(0b1100);
                direction(0b1000);
                direction(0b1001);
            }
        }
        else if (_direction == -1) {
            for (int i=0; i<_step; i++) {
                direction(0b1000);
                direction(0b1100);
                direction(0b0100);
                direction(0b0110);
                direction(0b0010);
                direction(0b0011);
                direction(0b0001);
                direction(0b1001);
            }
        }
        
    }

    private void direction(int _direction){

        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(DRIVER_I2C_ADD);

        i2cPayloadWriter.writeByte(DIR_REG);
        i2cPayloadWriter.writeByte(_direction);
        i2cPayloadWriter.writeByte(DUMMY_BYTE);

        target.i2cCommandClose(i2cPayloadWriter);
        target.i2cFlushBatch();
        target.i2cDelay(DRIVER_I2C_ADD, 4000000);
    }


}
