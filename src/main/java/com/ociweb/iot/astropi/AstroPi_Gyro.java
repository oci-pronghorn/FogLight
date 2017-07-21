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
public class AstroPi_Gyro {
    private final FogCommandChannel target;
    
    public AstroPi_Gyro(FogCommandChannel ch){
        this.target = ch;
    }
    
    private boolean enabled = true;
    private boolean enableX = true;
    private boolean enableY = true;
    private boolean enableZ = true;
    // scale can be set to either 245, 500, or 2000
    private int scale = 245;
    // [sampleRate] sets the output data rate (ODR) of the gyro
    // sampleRate can be set between 1-6
    // 1 = 14.9    4 = 238
    // 2 = 59.5    5 = 476
    // 3 = 119     6 = 952
    private int sampleRate = 3;
    // [bandwidth] can set the cutoff frequency of the gyro.
    // Allowed values: 0-3. Actual value of cutoff frequency
    // depends on the sample rate. (Datasheet section 7.12)
    private int bandwidth = 0;
    // [lowPowerEnable] turns low-power mode on or off.
    
    private boolean lowPowerEnable = false;
    // [HPFEnable] enables or disables the high-pass filter
    
    private boolean HPFEnable = false;
    // [HPFCutoff] sets the HPF cutoff frequency (if enabled)
    // Allowable values are 0-9. Value depends on ODR.
    // (Datasheet section 7.14)
    private int HPFCutoff = 0; //HPF cutoff = 4Hz
    // [flipX], [flipY], and [flipZ] are booleans that can
    // automatically switch the positive/negative orientation
    // of the three gyro axes.
    private boolean flipX = false;
    private boolean flipY = false;
    private boolean flipZ = false;
    
    private int orientation = 0;
    private boolean latchInterrupt = true;
    
    
    public void initGyro(){
        int tempRegValue = 0;
        if(enabled){
            tempRegValue = (sampleRate & 0x07) << 5;
        }
        switch(scale){
            case 500:
                tempRegValue |= (0x01 << 3);
                break;
            case 2000:
                tempRegValue |= (0x03 << 3);
                break;
        }
        tempRegValue |= bandwidth & 0x03;
        
        writeSingleByteToRegister(AstroPi_Constants.CTRL_REG1_G,tempRegValue);
        // CTRL_REG2_G (Default value: 0x00)
	// [0][0][0][0][INT_SEL1][INT_SEL0][OUT_SEL1][OUT_SEL0]
	// INT_SEL[1:0] - INT selection configuration
	// OUT_SEL[1:0] - Out selection configuration
        writeSingleByteToRegister(AstroPi_Constants.CTRL_REG2_G,0x00);
        // CTRL_REG3_G (Default value: 0x00)
	// [LP_mode][HP_EN][0][0][HPCF3_G][HPCF2_G][HPCF1_G][HPCF0_G]
	// LP_mode - Low-power mode enable (0: disabled, 1: enabled)
	// HP_EN - HPF enable (0:disabled, 1: enabled)
	// HPCF_G[3:0] - HPF cutoff frequency
	tempRegValue = lowPowerEnable ? (1<<7) : 0;
	if (HPFEnable)
	{
		tempRegValue |= (1<<6) | (HPFCutoff & 0x0F);
	}
	writeSingleByteToRegister(AstroPi_Constants.CTRL_REG3_G, tempRegValue);
	
	// CTRL_REG4 (Default value: 0x38)
	// [0][0][Zen_G][Yen_G][Xen_G][0][LIR_XL1][4D_XL1]
	// Zen_G - Z-axis output enable (0:disable, 1:enable)
	// Yen_G - Y-axis output enable (0:disable, 1:enable)
	// Xen_G - X-axis output enable (0:disable, 1:enable)
	// LIR_XL1 - Latched interrupt (0:not latched, 1:latched)
	// 4D_XL1 - 4D option on interrupt (0:6D used, 1:4D used)
	tempRegValue = 0;
	if (enableZ) tempRegValue |= (1<<5);
	if (enableY) tempRegValue |= (1<<4);
	if (enableX) tempRegValue |= (1<<3);
	if (latchInterrupt) tempRegValue |= (1<<1);
	writeSingleByteToRegister(AstroPi_Constants.CTRL_REG4, tempRegValue);
	
	// ORIENT_CFG_G (Default value: 0x00)
	// [0][0][SignX_G][SignY_G][SignZ_G][Orient_2][Orient_1][Orient_0]
	// SignX_G - Pitch axis (X) angular rate sign (0: positive, 1: negative)
	// Orient [2:0] - Directional user orientation selection
	tempRegValue = 0;
	if (flipX) tempRegValue |= (1<<5);
	if (flipY) tempRegValue |= (1<<4);
	if (flipZ) tempRegValue |= (1<<3);
	writeSingleByteToRegister(AstroPi_Constants.ORIENT_CFG_G, tempRegValue);
        
        target.i2cFlushBatch();


    }
    public void setGyroScale(int gScl){
        
    }
    
       private void writeSingleByteToRegister(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LSM9DS1_I2C_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
    }
}
