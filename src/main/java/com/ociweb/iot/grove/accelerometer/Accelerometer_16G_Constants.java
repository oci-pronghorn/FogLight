/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.accelerometer;

/**
 *
 * @author huydo
 */
public class Accelerometer_16G_Constants {
    public static final int ADXL345_DEVICE    = 0x53;
    
    public static final int ADXL345_POWER_CTL = 0x2D;
    public static final int ADXL345_DATA_FORMAT = 0x31; 
    public static final int ADXL345_DATAX0    = 0x32; 
    public static final int ADXL345_ACT_TAP_STATUS = 0x2b;
    
    public static final int ADXL345_OFSX = 0x1e;
    public static final int ADXL345_OFSY = 0x1f;
    public static final int ADXL345_OFSZ = 0x20;
    
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
