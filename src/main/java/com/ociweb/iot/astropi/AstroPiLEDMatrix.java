/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author huydo
 */
public class AstroPiLEDMatrix {
    private final FogCommandChannel target;
    public AstroPiLEDMatrix(FogCommandChannel ch){
        this.target = ch;
    }
    
    public void setPixel(int row,int col,int r,int g,int b){
        int redAddr = 24*row + col;
        int greenAddr = 24*row + 8 + col;
        int blueAddr = 24*row + 16 + col;
        writeSingleByteToRegister(redAddr,r);
        writeSingleByteToRegister(greenAddr,g);
        writeSingleByteToRegister(blueAddr,b);
    }
    public int[] bitmapToList(int[][][] map){
        int [] list = new int[192];
        int idx = 0;
        for(int ver = 0;ver<8;ver++){
            for(int color = 0;color<3;color++){
                for(int hor = 0;hor<8;hor++){
                    list[idx] = map[ver][hor][color];
                    idx++;
                }
            }
        }
        return list;
    }
    
    public void setPixels(int[] vals){
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);
        
        i2cPayloadWriter.writeByte(0);
        for(int i = 0;i<= 191;i++){
            i2cPayloadWriter.writeByte(vals[i]);
        }
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    public void clear(){
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);
        
        i2cPayloadWriter.writeByte(0);
        for(int i = 0;i<= 191;i++){
            i2cPayloadWriter.writeByte(0);
        }
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    
    public void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(0x46);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    
}
