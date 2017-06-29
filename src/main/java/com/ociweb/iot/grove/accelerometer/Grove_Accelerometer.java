/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.accelerometer;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

import static com.ociweb.iot.grove.accelerometer.Grove_Acc_Constants.*;

/**
 *
 * @author huydo
 */
public class Grove_Accelerometer implements IODevice{
    
    public static final Grove_Accelerometer instance = new Grove_Accelerometer();
    
    public static int pollingRate = 0;
    
    private Grove_Accelerometer(){
        
    }
    
    public static boolean begin(FogCommandChannel target) {
        
        
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,0);
        
        target.i2cFlushBatch();
        
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,16);
        
        target.i2cFlushBatch();
        
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_POWER_CTL,8);
        
        target.i2cFlushBatch();
        
        writeSingleByteToRegister(target, Grove_Acc_Constants.ADXL345_DEVICE, Grove_Acc_Constants.ADXL345_DATA_FORMAT,0x00);
        
        target.i2cFlushBatch();
        
        return true;
    }
    
    public static short[] intepretData(byte[] backing, int position, int length, int mask){
        assert(length==6) : "Non-Accelerometer data passed into the NunchuckTwig class";
        short[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (short)(((backing[(position+1)&mask]) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (short)(((backing[(position+3)&mask]) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (short)(((backing[(position+5)&mask]) << 8) | (backing[(position+4)&mask]&0xFF));

        return temp;
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
        byte[] ACC_READCMD = {ADXL345_DATAX0};
        //byte[] ACC_SETUP = {ADXL345_POWER_CTL,0x08};
        byte[] ACC_SETUP = {};
        byte ACC_ADDR = ADXL345_DEVICE;
        byte ACC_BYTESTOREAD = 6;
        byte ACC_REGISTER = 0x07; //just an identifier
        return new I2CConnection(this, ACC_ADDR, ACC_READCMD, ACC_BYTESTOREAD, ACC_REGISTER, ACC_SETUP);
    }
    
    
    @Override
    public int response() {
        if(pollingRate != 0){
            return pollingRate;
        }else{
        return 1000;
        }
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
	public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
