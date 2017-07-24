/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import static com.ociweb.iot.astropi.AstroPi_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class GyroSettings {
    
    protected static boolean enabled = true;
    protected static boolean enableX = true;
    protected static boolean enableY = true;
    protected static boolean enableZ = true;
    // scale can be set to either 245, 500, or 2000
    protected static int scale = 245;
    // [sampleRate] sets the output data rate (ODR) of the gyro
    // sampleRate can be set between 1-6
    // 1 = 14.9    4 = 238
    // 2 = 59.5    5 = 476
    // 3 = 119     6 = 952
    protected static int sampleRate = 3;
    // [bandwidth] can set the cutoff frequency of the gyro.
    // Allowed values: 0-3. Actual value of cutoff frequency
    // depends on the sample rate. (Datasheet section 7.12)
    protected static int bandwidth = 0;
    // [lowPowerEnable] turns low-power mode on or off.
    
    protected static boolean lowPowerEnable = false;
    // [HPFEnable] enables or disables the high-pass filter
    
    protected static boolean HPFEnable = false;
    // [HPFCutoff] sets the HPF cutoff frequency (if enabled)
    // Allowable values are 0-9. Value depends on ODR.
    // (Datasheet section 7.14)
    protected static int HPFCutoff = 0; //HPF cutoff = 4Hz
    // [flipX], [flipY], and [flipZ] are booleans that can
    // automatically switch the positive/negative orientation
    // of the three gyro axes.
    protected static boolean flipX = false;
    protected static boolean flipY = false;
    protected static boolean flipZ = false;
    
    protected static int orientation = 0;
    protected static boolean latchInterrupt = true;
    
    protected static int CTRL_REG1_GVal = 0x00;
    protected static int CTRL_REG2_GVal = 0x00;
    protected static int CTRL_REG3_GVal = 0x00;
    protected static int CTRL_REG4_GVal = 0x00;
    protected static int ORIENT_CFG_GVal = 0x00;
    
    protected static double gRes;
    
    protected static int[] gBias = new int[3];
    protected static int[] gBiasRaw = new int[3];
}
