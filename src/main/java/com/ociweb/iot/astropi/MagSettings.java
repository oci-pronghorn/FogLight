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
public class MagSettings {
    // Magnetometer settings:
    protected static boolean enabled =true;
    // mag scale can be 4, 8, 12, or 16
    protected static int scale =4;
    // mag data rate can be 0-7
    // 0 = 0.625 Hz  4 = 10 Hz
    // 1 = 1.25 Hz   5 = 20 Hz
    // 2 = 2.5 Hz    6 = 40 Hz
    // 3 = 5 Hz      7 = 80 Hz
    
    protected static int sampleRate =7;
    // New mag stuff:
    protected static boolean tempCompensationEnable =false;
    // magPerformance can be any value between 0-3
    // 0 = Low power mode      2 = high performance
    // 1 = medium performance  3 = ultra-high performance
    protected static int XYPerformance =3;
    protected static int ZPerformance=3;
    protected static boolean lowPowerEnable = false;
    // magOperatingMode can be 0-2
    // 0 = continuous conversion
    // 1 = single-conversion
    // 2 = power down
    protected static int operatingMode =0;
    protected static int CTRL_REG1_MVal = 0;
    protected static int CTRL_REG2_MVal = 0;
    protected static int CTRL_REG3_MVal = 0;
    protected static int CTRL_REG4_MVal = 0;
    protected static int CTRL_REG5_MVal = 0;
    
    protected static double mRes;
    
    protected static int[] mBias = new int[3];
    protected static int[] mBiasRaw = new int[3];
}
