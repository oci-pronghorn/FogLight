/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.I2CIODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author huydo
 */
public class AstroPiLEDMatrix2 implements I2CIODevice {
    private  FogCommandChannel target;
    public byte address;
    public AstroPiLEDMatrix2(FogCommandChannel ch){
        this.target = ch;
    }
    public AstroPiLEDMatrix2(byte add){
        this.address = add;
    }
    
    public void test(){
        int intensity = 63;
        for(int i = 0;i<= 191;i++){
            //System.out.println(i);
            writeSingleByteToRegister(i,intensity);
            target.i2cDelay(0x46, 500000000);
        }
    }
    public void clear(){
 
        for(int i = 0;i<= 191;i++){
            writeSingleByteToRegister(i,0);

        }
    }
    
    
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);

        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
        
        
    }
    public void writeMultipleBytesToRegister(int register) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);

        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(63);
        i2cPayloadWriter.writeByte(63);
        i2cPayloadWriter.writeByte(63);
        i2cPayloadWriter.writeByte(63);
        target.i2cCommandClose();
        target.i2cFlushBatch();
        
        
    }

    @Override
    public int response() {
        return 500;
    }

    @Override
    public int scanDelay() {
        return 0;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int range() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public I2CConnection getI2CConnection() {
         byte[] REG_ADDR = {address};
            byte[] SETUP = {};
            byte I2C_ADDR = 0x46;
            byte BYTESTOREAD = 1;
            byte REG_ID = address; //just an identifier
            return new I2CConnection(this, I2C_ADDR, REG_ADDR, BYTESTOREAD, REG_ID, SETUP);
    }

    @Override
    public boolean isValid(byte[] backing, int position, int length, int mask) {
        return true;
    }

    @Override
    public int pinsUsed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
