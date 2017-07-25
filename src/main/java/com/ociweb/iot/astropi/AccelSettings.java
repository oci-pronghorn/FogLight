/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.astropi;

/**
 *
 * @author huydo
 */
public class AccelSettings {
    // Accelerometer settings:
    protected static boolean enabled =true;
    // accel scale can be 2, 4, 8, or 16
    protected static int scale = 2;
    // accel sample rate can be 1-6
	// 1 = 10 Hz    4 = 238 Hz
	// 2 = 50 Hz    5 = 476 Hz
	// 3 = 119 Hz   6 = 952 Hz
    protected static int sampleRate = 6;
    // New accel stuff:
    protected static boolean enableX = true;
    protected static boolean enableY= true;
    protected static boolean enableZ= true;
    // Accel cutoff frequency can be any value between -1 and 3. 
	// -1 = bandwidth determined by sample rate
	// 0 = 408 Hz   2 = 105 Hz
	// 1 = 211 Hz   3 = 50 Hz
    protected static int  bandwidth = -1;
    protected static boolean highResEnable = false;
    // accelHighResBandwidth can be any value between 0-3
	// LP cutoff is set to a factor of sample rate
	// 0 = ODR/50    2 = ODR/9
	// 1 = ODR/100   3 = ODR/400
    
    protected static int highResBandwidth = 0;
    
    protected static int CTRL_REG5_XLVal = 0;
    protected static int CTRL_REG6_XLVal = 0;
    protected static int CTRL_REG7_XLVal = 0;
    
    protected static double aRes;
    
    protected static double[] aBias = new double[3];
    protected static int[] aBiasRaw = new int[3];
}
