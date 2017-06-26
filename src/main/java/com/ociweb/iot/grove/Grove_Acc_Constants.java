/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove;

/**
 *
 * @author huydo
 */
public class Grove_Acc_Constants {
    public static final int ADXL345_POWER_CTL = 0x2d;
    public static final int ADXL345_DEVICE    = 0x53;
    public static final int ADXL345_DATAX0    = 0x32;

    public static final byte WRITE_BIT = (byte)(0xA6 & 0xFF);
    public static final byte READ_BIT = (byte)(0xA7 & 0xFF);

    public static final double X_GAIN = 0.00376390;
    public static final double Y_GAIN = 0.00376009;
    public static final double Z_GAIN = 0.00349265;
}
