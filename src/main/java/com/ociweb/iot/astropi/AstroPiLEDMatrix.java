/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.astropi;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class AstroPiLEDMatrix {
    private final FogCommandChannel target;
    public AstroPiLEDMatrix(FogCommandChannel ch){
        this.target = ch;
    }
    
    public void test(){
        int[] pix ={128,128,128}; 
        int r = (pix[0] >> 3) & 0x1F;
        int g = (pix[1] >> 2) & 0x3F;
        int b = (pix[2] >> 3) & 0x1F;
        int bits16 = (r << 11) + (g << 5) + b;
        for(int i = 0;i<= 255;i++){
            writeSingleByteToRegister(i,bits16);
        }
    }
    
    
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    

}
