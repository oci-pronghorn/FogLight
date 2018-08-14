/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.three_axis_accelerometer_16g;

/**
 *
 * @author huydo
 */
public class ThreeAxisAccelerometer_16g_Constants {
    public static final int ADXL345_DEVICE    = 0x53;
    
    public static final int ADXL345_POWER_CTL = 0x2D;
    public static final int ADXL345_DATA_FORMAT = 0x31;
    
    public static final int ADXL345_DATAX0    = 0x32;
    public static final int ADXL345_DATAX1 = 0x33;
    public static final int ADXL345_DATAY0 = 0x34;
    public static final int ADXL345_DATAY1 = 0x35;
    public static final int ADXL345_DATAZ0 = 0x36;
    public static final int ADXL345_DATAZ1 = 0x37;
    
    
    
    public static final int ADXL345_WINDOW = 0x23;
    public static final int ADXL345_LATENT = 0x22;
    public static final int ADXL345_DUR = 0x21;
    public static final int ADXL345_THRESH_TAP = 0x1d;
    public static final int ADXL345_ACT_TAP_STATUS = 0x2b;
    public static final int ADXL345_OFSX = 0x1e;
    public static final int ADXL345_OFSY = 0x1f;
    public static final int ADXL345_OFSZ = 0x20;
    
    public static final int ADXL345_DEVID = 0x00;
    public static final int ADXL345_RESERVED1 = 0x01;
    
    public static final int ADXL345_THRESH_ACT = 0x24;
    public static final int ADXL345_THRESH_INACT = 0x25;
    public static final int ADXL345_TIME_INACT = 0x26;
    public static final int ADXL345_ACT_INACT_CTL = 0x27;
    public static final int ADXL345_THRESH_FF = 0x28;
    public static final int ADXL345_TIME_FF = 0x29;
    public static final int ADXL345_TAP_AXES = 0x2a;
    public static final int ADXL345_BW_RATE = 0x2c;
    public static final int ADXL345_INT_ENABLE = 0x2e;
    public static final int ADXL345_INT_MAP = 0x2f;
    public static final int ADXL345_INT_SOURCE = 0x30;
    
    public static final int ADXL345_FIFO_CTL = 0x38;
    public static final int ADXL345_FIFO_STATUS = 0x39;
    
    public static final int ADXL345_RATE_3200 = 0x0F; // 1111
    public static final int ADXL345_RATE_1600 = 0x0E; // 1110
    public static final int ADXL345_RATE_800 = 0x0D ;// 1101
    public static final int ADXL345_RATE_400 = 0x0C; // 1100
    public static final int ADXL345_RATE_200 = 0x0B; // 1011
    public static final int ADXL345_RATE_100 = 0x0A ;// 1010
    public static final int ADXL345_RATE_50 = 0x09 ;// 1001
    public static final int ADXL345_RATE_25 = 0x08 ;// 1000
    public static final int ADXL345_RATE_12 = 0x07; // 0111
    public static final int ADXL345_RATE_6 = 0x06; // 0110
}
