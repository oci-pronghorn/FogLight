/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;


/**
 *
 * @author huydo
 */
public class AstroPi implements I2CListener {
    private final FogCommandChannel target;
    private int[][][] bitmap = new int [8][8][3];
    
    public AstroPi(FogCommandChannel ch,AstroPiListener... l){
        this.target = ch;
        for(AstroPiListener item:l){
            if(item instanceof JoyStickListener){
                this.joysticklistener = (JoyStickListener) item;
            }
        }
    }
    
    /*      LED MATRIX CONTROLS                                       */
    
    /**
     * Set the color of one pixel at the specified position. (0,0) is in the top left
     * corner of the screen with the Pi's hdmi port pointing down
     * @param row integer from 0 to 7 , row index
     * @param col integer from 0 to 7 , column index
     * @param r integer from 0 to 63, intensity of red
     * @param g integer from 0 to 63, intensity of green
     * @param b integer from 0 to 63, intensity of blue
     */
    private void setPixel(int row,int col,int r,int g,int b){
        row = ensureRange(row,0,7);
        col = ensureRange(col,0,7);
        int redAddr = 24*row + col;
        int greenAddr = 24*row + 8 + col;
        int blueAddr = 24*row + 16 + col;
        
        int [] addr = {redAddr,greenAddr,blueAddr};
        int [] rgb = {r,g,b};
        drawPixel(addr,rgb);
        
        bitmap[row][col][0] = r;
        bitmap[row][col][1] = g;
        bitmap[row][col][2] = b;
    }
    /**
     * Set the color of one pixel at the specified position. (0,0) is in the top left
     * corner of the screen with the Pi's hdmi port pointing down
     * @param row integer from 0 to 7 , row index
     * @param col integer from 0 to 7 , col index
     * @param rgb array of rgb intensity values which must be integer from 0 to 63
     *
     */
    public void setPixel(int row,int col,int[] rgb){
        row = ensureRange(row,0,7);
        col = ensureRange(col,0,7);
        int redAddr = 24*row + col;
        int greenAddr = 24*row + 8 + col;
        int blueAddr = 24*row + 16 + col;
        
        int [] addr = {redAddr,greenAddr,blueAddr};
        drawPixel(addr,rgb);
        
        bitmap[row][col][0] = rgb[0];
        bitmap[row][col][1] = rgb[1];
        bitmap[row][col][2] = rgb[2];
        
    }
    /**
     * Set the entire screen
     * @param matrix 8x8x3 matrix
     */
    public void setPixels(int[][][] matrix){
        bitmap = copyBitmap(matrix);
        drawPixels(bitmapToList(bitmap));
    }
    /**
     * get the 8x8x3 matrix showing the current state of the screen
     * @return 8x8x3 matrix showing the current state of the screen
     */
    public int[][][] getPixels(){
        return bitmap;
    }
    /**
     * get the R,G,B values of the specified pixel
     * @param row integer from 0 to 7
     * @param col integer from 0 to 7
     * @return R,G,B values of the specified pixel
     */
    public int[] getPixel(int row,int col){
        row = ensureRange(row,0,7);
        col = ensureRange(col,0,7);
        return bitmap[row][col];
    }
    /**
     * Clear the screen
     * @return an 8x8x3 3-dimension array of 0s.
     */
    public int[][][] clear(){
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LED_I2C_ADDR);
        
        i2cPayloadWriter.writeByte(0);
        for(int i = 0;i<= 191;i++){
            i2cPayloadWriter.writeByte(0);
        }
        target.i2cCommandClose();
        target.i2cFlushBatch();
        for(int ver = 0;ver<8;ver++){
            for(int color = 0;color<3;color++){
                for(int hor = 0;hor<8;hor++){
                    bitmap[ver][hor][color] = 0;
                }
            }
        }
        return bitmap;
    }
    /**
     * Flips the image on the LED matrix horizontally.
     */
    public void flip_h(){
        for(int i = 0;i < 8;i++){
            for(int j = 0;j < 4;j++){
                int[] temp = bitmap[i][j];
                bitmap[i][j] = bitmap[i][7-j];
                bitmap[i][7-j] = temp;
            }
        }
        drawPixels(bitmapToList(bitmap));
    }
    /**
     * Flips the image on the LED matrix vertically
     */
    public void flip_v(){
        for(int i = 0;i < 8;i++){
            for(int j = 0;j < 4;j++){
                int[] temp = bitmap[j][i];
                bitmap[j][i] = bitmap[7-j][i];
                bitmap[7-j][i] = temp;
            }
        }
        drawPixels(bitmapToList(bitmap));
    }
    
    public void setRotation(int angle){
        switch (angle) {
            case 90:
                bitmap = rotateCW90(bitmap);
                break;
            case 180:
                bitmap = rotateCW90(rotateCW90(bitmap));
                break;
            case 270:
                bitmap = rotateCW90(rotateCW90(rotateCW90(bitmap)));
                break;
            default:
                break;
        }
        
        drawPixels(bitmapToList(bitmap));
    }
    
    private int ensureRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
    
    
    private int[] bitmapToList(int[][][] map){
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
    
    private int[][][] rotateCW90(int[][][] matrix) {
        
        int low =0,high = 7;
        while(low < high){
            int[][] temp = matrix[low];
            matrix[low] = matrix[high];
            matrix[high] = temp;
            low++;
            high--;
        }
        
        for(int i=0; i<8; i++){
            for(int j=0; j<i; j++){
                int[] temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }
        return matrix;
    }
    private int [][][] copyBitmap(int[][][] arr){
        for(int ver = 0;ver<8;ver++){
            for(int color = 0;color<3;color++){
                for(int hor = 0;hor<8;hor++){
                    bitmap[ver][hor][color] = arr[ver][hor][color];
                }
            }
        }
        return bitmap;
    }
    private void drawPixel(int[] addr,int[] intensity){
        for(int i = 0;i<3;i++){
            DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LED_I2C_ADDR);
            
            i2cPayloadWriter.writeByte(addr[i]);
            i2cPayloadWriter.writeByte(intensity[i]);
            
            target.i2cCommandClose();
            
        }
        target.i2cFlushBatch();
    }
    /**
     * Draw the whole screen
     * @param vals
     */
    private void drawPixels(int[] vals){
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LED_I2C_ADDR);
        
        i2cPayloadWriter.writeByte(0);
        for(int i = 0;i<= 191;i++){
            i2cPayloadWriter.writeByte(vals[i]);
        }
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    JoyStickListener joysticklistener;
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == AstroPi_Constants.LED_I2C_ADDR){
            if(register == AstroPi_Constants.JOYSTICK_REG_ADDR){
                int down = (backing[position]&0x01);
                int right = (backing[position]&0x02)>>1;
                int up = (backing[position]&0x04)>>2;
                int push = (backing[position]&0x08)>>3;
                int left = (backing[position]&0x1f)>>4;
                System.out.println(down);
                joysticklistener.joystickEvent(up, down, left, right, push);
            }
        }
    }
    
    /*   LSM9DS1 3D accelerometer, 3D gyroscope, 3D magnetometer  */
    public void initGyro(){
        
    }
    
    
    
}
