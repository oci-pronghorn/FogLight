/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.mini_i2c_motor;

import static com.ociweb.iot.grove.mini_i2c_motor.Grove_Mini_I2CMotor_Constants.*;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class Grove_Mini_I2CMotor implements IODevice {
    
    public static final Grove_Mini_I2CMotor instance = new Grove_Mini_I2CMotor();
    
    private Grove_Mini_I2CMotor(){
        
    }
    public static Mini_I2C_Motor newObj(FogCommandChannel ch){
		return new Mini_I2C_Motor(ch);
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
        byte[] MOTOR_SETUP = {};
        byte MOTOR_ADDR = CH1_ADD;
        byte MOTOR_BYTESTOREAD = 1;
        byte MOTOR_REGISTER = 0x23;  //register identifier
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
	@Override
	public <F extends Facade> F newFacade(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
}
