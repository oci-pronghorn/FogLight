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
public class AstroPi_IMU {
    FogCommandChannel target;
    private int CTRL_REG9Val;
    private boolean tempEnable = true;
    private boolean _autoCalc = false;
    
    public AstroPi_IMU(FogCommandChannel ch){
        this.target = ch;
    }
    
    public void init(){
        
        tempEnable = true;
        for (int i=0; i<3; i++)
	{
		GyroSettings.gBias[i] = 0;
		AccelSettings.aBias[i] = 0;
		MagSettings.mBias[i] = 0;
		GyroSettings.gBiasRaw[i] = 0;
		AccelSettings.aBiasRaw[i] = 0;
		MagSettings.mBiasRaw[i] = 0;
	}
	_autoCalc = false;
        
    }
    
    public void begin(){
        constrainScales();
        // Once we have the scale values, we can calculate the resolution
	// of each sensor. That's what these functions are for. One for each sensor
	calcgRes(); // Calculate DPS / ADC tick, stored in gRes variable
	calcmRes(); // Calculate Gs / ADC tick, stored in mRes variable
	calcaRes(); // Calculate g / ADC tick, stored in aRes variable
        
        initGyro();
        initAccel();
        initMag();
        
    }
    
    private void constrainScales()
    {
        if ((GyroSettings.scale != 245) && (GyroSettings.scale != 500) &&
                (GyroSettings.scale != 2000))
        {
            GyroSettings.scale = 245;
        }
        
        if ((AccelSettings.scale != 2) && (AccelSettings.scale != 4) &&
                (AccelSettings.scale != 8) && (AccelSettings.scale != 16))
        {
            AccelSettings.scale = 2;
        }
        
        if ((MagSettings.scale != 4) && (MagSettings.scale != 8) &&
                (MagSettings.scale != 12) && (MagSettings.scale != 16))
        {
            MagSettings.scale = 4;
        }
    }
    public void initGyro(){
        if(GyroSettings.enabled){
            GyroSettings.CTRL_REG1_GVal = (GyroSettings.sampleRate & 0x07) << 5;
        }
        switch(GyroSettings.scale){
            case 500:
                GyroSettings.CTRL_REG1_GVal |= (0x01 << 3);
                break;
            case 2000:
                GyroSettings.CTRL_REG1_GVal |= (0x03 << 3);
                break;
        }
        GyroSettings.CTRL_REG1_GVal |= GyroSettings.bandwidth & 0x03;
        
        agWriteByte(AstroPi_Constants.CTRL_REG1_G,GyroSettings.CTRL_REG1_GVal);
        // CTRL_REG2_G (Default value: 0x00)
        // [0][0][0][0][INT_SEL1][INT_SEL0][OUT_SEL1][OUT_SEL0]
        // INT_SEL[1:0] - INT selection configuration
        // OUT_SEL[1:0] - Out selection configuration
        agWriteByte(AstroPi_Constants.CTRL_REG2_G,GyroSettings.CTRL_REG2_GVal);
        // CTRL_REG3_G (Default value: 0x00)
        // [LP_mode][HP_EN][0][0][HPCF3_G][HPCF2_G][HPCF1_G][HPCF0_G]
        // LP_mode - Low-power mode enable (0: disabled, 1: enabled)
        // HP_EN - HPF enable (0:disabled, 1: enabled)
        // HPCF_G[3:0] - HPF cutoff frequency
        GyroSettings.CTRL_REG3_GVal = GyroSettings.lowPowerEnable ? (1<<7) : 0;
        if (GyroSettings.HPFEnable)
        {
            GyroSettings.CTRL_REG3_GVal |= (1<<6) | (GyroSettings.HPFCutoff & 0x0F);
        }
        agWriteByte(AstroPi_Constants.CTRL_REG3_G, GyroSettings.CTRL_REG3_GVal);
        
        // CTRL_REG4 (Default value: 0x38)
        // [0][0][Zen_G][Yen_G][Xen_G][0][LIR_XL1][4D_XL1]
        // Zen_G - Z-axis output enable (0:disable, 1:enable)
        // Yen_G - Y-axis output enable (0:disable, 1:enable)
        // Xen_G - X-axis output enable (0:disable, 1:enable)
        // LIR_XL1 - Latched interrupt (0:not latched, 1:latched)
        // 4D_XL1 - 4D option on interrupt (0:6D used, 1:4D used)
        GyroSettings.CTRL_REG4_GVal = 0;
        if (GyroSettings.enableZ) GyroSettings.CTRL_REG4_GVal |= (1<<5);
        if (GyroSettings.enableY) GyroSettings.CTRL_REG4_GVal |= (1<<4);
        if (GyroSettings.enableX) GyroSettings.CTRL_REG4_GVal |= (1<<3);
        if (GyroSettings.latchInterrupt) GyroSettings.CTRL_REG4_GVal |= (1<<1);
        agWriteByte(AstroPi_Constants.CTRL_REG4, GyroSettings.CTRL_REG4_GVal);
        
        // ORIENT_CFG_G (Default value: 0x00)
        // [0][0][SignX_G][SignY_G][SignZ_G][Orient_2][Orient_1][Orient_0]
        // SignX_G - Pitch axis (X) angular rate sign (0: positive, 1: negative)
        // Orient [2:0] - Directional user orientation selection
        GyroSettings.ORIENT_CFG_GVal = 0;
        if (GyroSettings.flipX) GyroSettings.ORIENT_CFG_GVal |= (1<<5);
        if (GyroSettings.flipY) GyroSettings.ORIENT_CFG_GVal |= (1<<4);
        if (GyroSettings.flipZ) GyroSettings.ORIENT_CFG_GVal |= (1<<3);
        agWriteByte(AstroPi_Constants.ORIENT_CFG_G, GyroSettings.ORIENT_CFG_GVal);
        
        target.i2cFlushBatch();
        
        
    }
        
    public void initAccel(){
        AccelSettings.CTRL_REG5_XLVal = 0;
        
        //	CTRL_REG5_XL (0x1F) (Default value: 0x38)
        //	[DEC_1][DEC_0][Zen_XL][Yen_XL][Zen_XL][0][0][0]
        //	DEC[0:1] - Decimation of accel data on OUT REG and FIFO.
        //		00: None, 01: 2 samples, 10: 4 samples 11: 8 samples
        //	Zen_XL - Z-axis output enabled
        //	Yen_XL - Y-axis output enabled
        //	Xen_XL - X-axis output enabled
        if (AccelSettings.enableZ) AccelSettings.CTRL_REG5_XLVal |= (1<<5);
        if (AccelSettings.enableY) AccelSettings.CTRL_REG5_XLVal |= (1<<4);
        if (AccelSettings.enableX) AccelSettings.CTRL_REG5_XLVal |= (1<<3);
        
        agWriteByte(CTRL_REG5_XL, AccelSettings.CTRL_REG5_XLVal);
        
        // CTRL_REG6_XL (0x20) (Default value: 0x00)
        // [ODR_XL2][ODR_XL1][ODR_XL0][FS1_XL][FS0_XL][BW_SCAL_ODR][BW_XL1][BW_XL0]
        // ODR_XL[2:0] - Output data rate & power mode selection
        // FS_XL[1:0] - Full-scale selection
        // BW_SCAL_ODR - Bandwidth selection
        // BW_XL[1:0] - Anti-aliasing filter bandwidth selection
        AccelSettings.CTRL_REG6_XLVal = 0;
        // To disable the accel, set the sampleRate bits to 0.
        if (AccelSettings.enabled)
        {
            AccelSettings.CTRL_REG6_XLVal |= (AccelSettings.sampleRate & 0x07) << 5;
        }
        switch (AccelSettings.scale)
        {
            case 4:
                AccelSettings.CTRL_REG6_XLVal |= (0x2 << 3);
                break;
            case 8:
                AccelSettings.CTRL_REG6_XLVal |= (0x3 << 3);
                break;
            case 16:
                AccelSettings.CTRL_REG6_XLVal |= (0x1 << 3);
                break;
                // Otherwise it'll be set to 2g (0x0 << 3)
        }
        if (AccelSettings.bandwidth >= 0)
        {
            AccelSettings.CTRL_REG6_XLVal |= (1<<2); // Set BW_SCAL_ODR
            AccelSettings.CTRL_REG6_XLVal |= (AccelSettings.bandwidth & 0x03);
        }
        agWriteByte(CTRL_REG6_XL, AccelSettings.CTRL_REG6_XLVal);
        
        // CTRL_REG7_XL (0x21) (Default value: 0x00)
        // [HR][DCF1][DCF0][0][0][FDS][0][HPIS1]
        // HR - High resolution mode (0: disable, 1: enable)
        // DCF[1:0] - Digital filter cutoff frequency
        // FDS - Filtered data selection
        // HPIS1 - HPF enabled for interrupt function
        AccelSettings.CTRL_REG7_XLVal = 0;
        if (AccelSettings.highResEnable)
        {
            AccelSettings.CTRL_REG7_XLVal |= (1<<7); // Set HR bit
            AccelSettings.CTRL_REG7_XLVal |= (AccelSettings.highResBandwidth & 0x3) << 5;
        }
        agWriteByte(CTRL_REG7_XL, AccelSettings.CTRL_REG7_XLVal);
        
        target.i2cFlushBatch();
    }
    
    public void initMag()
    {
        MagSettings.CTRL_REG1_MVal = 0;
        
        // CTRL_REG1_M (Default value: 0x10)
        // [TEMP_COMP][OM1][OM0][DO2][DO1][DO0][0][ST]
        // TEMP_COMP - Temperature compensation
        // OM[1:0] - X & Y axes op mode selection
        //	00:low-power, 01:medium performance
        //	10: high performance, 11:ultra-high performance
        // DO[2:0] - Output data rate selection
        // ST - Self-test enable
        if (MagSettings.tempCompensationEnable) MagSettings.CTRL_REG1_MVal |= (1<<7);
        MagSettings.CTRL_REG1_MVal |= (MagSettings.XYPerformance & 0x3) << 5;
        MagSettings.CTRL_REG1_MVal |= (MagSettings.sampleRate & 0x7) << 2;
        mWriteByte(CTRL_REG1_M, MagSettings.CTRL_REG1_MVal);
        
        // CTRL_REG2_M (Default value 0x00)
        // [0][FS1][FS0][0][REBOOT][SOFT_RST][0][0]
        // FS[1:0] - Full-scale configuration
        // REBOOT - Reboot memory content (0:normal, 1:reboot)
        // SOFT_RST - Reset config and user registers (0:default, 1:reset)
        MagSettings.CTRL_REG2_MVal = 0;
        switch (MagSettings.scale)
        {
            case 8:
                MagSettings.CTRL_REG2_MVal |= (0x1 << 5);
                break;
            case 12:
                MagSettings.CTRL_REG2_MVal |= (0x2 << 5);
                break;
            case 16:
                MagSettings.CTRL_REG2_MVal |= (0x3 << 5);
                break;
                // Otherwise we'll default to 4 gauss (00)
        }
        mWriteByte(CTRL_REG2_M, MagSettings.CTRL_REG2_MVal); // +/-4Gauss
        
        // CTRL_REG3_M (Default value: 0x03)
        // [I2C_DISABLE][0][LP][0][0][SIM][MD1][MD0]
        // I2C_DISABLE - Disable I2C interace (0:enable, 1:disable)
        // LP - Low-power mode cofiguration (1:enable)
        // SIM - SPI mode selection (0:write-only, 1:read/write enable)
        // MD[1:0] - Operating mode
        //	00:continuous conversion, 01:single-conversion,
        //  10,11: Power-down
        MagSettings.CTRL_REG3_MVal = 0;
        if (MagSettings.lowPowerEnable) MagSettings.CTRL_REG3_MVal |= (1<<5);
        MagSettings.CTRL_REG3_MVal |= (MagSettings.operatingMode & 0x3);
        mWriteByte(CTRL_REG3_M, MagSettings.CTRL_REG2_MVal); // Continuous conversion mode
        
        // CTRL_REG4_M (Default value: 0x00)
        // [0][0][0][0][OMZ1][OMZ0][BLE][0]
        // OMZ[1:0] - Z-axis operative mode selection
        //	00:low-power mode, 01:medium performance
        //	10:high performance, 10:ultra-high performance
        // BLE - Big/little endian data
        MagSettings.CTRL_REG4_MVal = 0;
        MagSettings.CTRL_REG4_MVal = (MagSettings.ZPerformance & 0x3) << 2;
        mWriteByte(CTRL_REG4_M, MagSettings.CTRL_REG4_MVal);
        
        // CTRL_REG5_M (Default value: 0x00)
        // [0][BDU][0][0][0][0][0][0]
        // BDU - Block data update for magnetic data
        //	0:continuous, 1:not updated until MSB/LSB are read
        MagSettings.CTRL_REG5_MVal = 0;
        mWriteByte(CTRL_REG5_M, MagSettings.CTRL_REG5_MVal);
        target.i2cFlushBatch();
    }
    
    public double calcGyro(int gyro)
    {
        // Return the gyro raw reading times our pre-calculated DPS / (ADC tick):
        return GyroSettings.gRes * gyro;
    }
    
    public double calcAccel(int accel)
    {
        // Return the accel raw reading times our pre-calculated g's / (ADC tick):
        return AccelSettings.aRes * accel;
    }
    
    public double calcMag(int mag)
    {
        // Return the mag raw reading times our pre-calculated Gs / (ADC tick):
        return MagSettings.mRes * mag;
    }
    public void setGyroScale(int gScl){
        GyroSettings.CTRL_REG1_GVal &= 0xE7;
        switch (gScl)
        {
            case 500:
                GyroSettings.CTRL_REG1_GVal |= (0x1 << 3);
                GyroSettings.scale = 500;
                break;
            case 2000:
                GyroSettings.CTRL_REG1_GVal |= (0x3 << 3);
                GyroSettings.scale = 2000;
                break;
            default: // Otherwise we'll set it to 245 dps (0x0 << 4)
                GyroSettings.scale = 245;
                break;
        }
        agWriteByte(AstroPi_Constants.CTRL_REG1_G, GyroSettings.CTRL_REG1_GVal);
        target.i2cFlushBatch();
        
        calcgRes();
    }
    
    public void setAccelScale(int aScl)
    {
        
        // Mask out accel scale bits:
        AccelSettings.CTRL_REG6_XLVal &= 0xE7;
        
        switch (aScl)
        {
            case 4:
                AccelSettings.CTRL_REG6_XLVal |= (0x2 << 3);
                AccelSettings.scale = 4;
                break;
            case 8:
                AccelSettings.CTRL_REG6_XLVal |= (0x3 << 3);
                AccelSettings.scale = 8;
                break;
            case 16:
                AccelSettings.CTRL_REG6_XLVal |= (0x1 << 3);
                AccelSettings.scale = 16;
                break;
            default: // Otherwise it'll be set to 2g (0x0 << 3)
                AccelSettings.scale = 2;
                break;
        }
        agWriteByte(CTRL_REG6_XL, AccelSettings.CTRL_REG6_XLVal);
        target.i2cFlushBatch();
        // Then calculate a new aRes, which relies on aScale being set correctly:
        calcaRes();
    }
    public void setMagScale(int mScl)
    {
        // Then mask out the mag scale bits:
        MagSettings.CTRL_REG2_MVal &= 0xFF^(0x3 << 5);
        
        switch (mScl)
        {
            case 8:
                MagSettings.CTRL_REG2_MVal |= (0x1 << 5);
                MagSettings.scale = 8;
                break;
            case 12:
                MagSettings.CTRL_REG2_MVal |= (0x2 << 5);
                MagSettings.scale = 12;
                break;
            case 16:
                MagSettings.CTRL_REG2_MVal |= (0x3 << 5);
                MagSettings.scale = 16;
                break;
            default: // Otherwise we'll default to 4 gauss (00)
                MagSettings.scale = 4;
                break;
        }
        
        // And write the new register value back into CTRL_REG6_XM:
        mWriteByte(CTRL_REG2_M, MagSettings.CTRL_REG2_MVal);
        target.i2cFlushBatch();
        
        // Calculate a new mRes, which relies on mScale being set correctly:
        calcmRes();
    }
    
    public void setGyroODR(int gRate){
        if((gRate & 0x07) != 0){
            //mask out the gyro ODR bits
            GyroSettings.CTRL_REG1_GVal &= 0xff ^ (0x07 <<5);
            GyroSettings.CTRL_REG1_GVal |= (gRate & 0x07) << 5;
            
            GyroSettings.sampleRate = gRate & 0x07;
            agWriteByte(AstroPi_Constants.CTRL_REG1_G, GyroSettings.CTRL_REG1_GVal);
            target.i2cFlushBatch();
        }
    }
    public void setAccelODR(int aRate)
    {
        // Only do this if aRate is not 0 (which would disable the accel)
        if ((aRate & 0x07) != 0)
        {
            
            // Then mask out the accel ODR bits:
            AccelSettings.CTRL_REG6_XLVal &= 0x1F;
            // Then shift in our new ODR bits:
            AccelSettings.CTRL_REG6_XLVal |= ((aRate & 0x07) << 5);
            AccelSettings.sampleRate = aRate & 0x07;
            // And write the new register value back into CTRL_REG1_XM:
            agWriteByte(CTRL_REG6_XL, AccelSettings.CTRL_REG6_XLVal);
            target.i2cFlushBatch();
        }
    }
    
    public void setMagODR(int mRate)
    {
        
        // Then mask out the mag ODR bits:
        MagSettings.CTRL_REG1_MVal &= 0xFF^(0x7 << 2);
        // Then shift in our new ODR bits:
        MagSettings.CTRL_REG1_MVal |= ((mRate & 0x07) << 2);
        MagSettings.sampleRate = mRate & 0x07;
        // And write the new register value back into CTRL_REG5_XM:
        mWriteByte(CTRL_REG1_M, MagSettings.CTRL_REG1_MVal);
        target.i2cFlushBatch();
        
    }
    
    
    public void calcgRes()
    {
        switch (GyroSettings.scale)
        {
            case 245:
                GyroSettings.gRes = SENSITIVITY_GYROSCOPE_245;
                break;
            case 500:
                GyroSettings.gRes = SENSITIVITY_GYROSCOPE_500;
                break;
            case 2000:
                GyroSettings.gRes = SENSITIVITY_GYROSCOPE_2000;
                break;
            default:
                break;
        }
    }
    public void calcaRes()
    {
        switch (AccelSettings.scale)
        {
            case 2:
                AccelSettings.aRes = SENSITIVITY_ACCELEROMETER_2;
                break;
            case 4:
                AccelSettings.aRes = SENSITIVITY_ACCELEROMETER_4;
                break;
            case 8:
                AccelSettings.aRes = SENSITIVITY_ACCELEROMETER_8;
                break;
            case 16:
                AccelSettings.aRes = SENSITIVITY_ACCELEROMETER_16;
                break;
            default:
                break;
        }
    }
    
    public void calcmRes()
    {
        switch (MagSettings.scale)
        {
            case 4:
                MagSettings.mRes = SENSITIVITY_MAGNETOMETER_4;
                break;
            case 8:
                MagSettings.mRes = SENSITIVITY_MAGNETOMETER_8;
                break;
            case 12:
                MagSettings.mRes = SENSITIVITY_MAGNETOMETER_12;
                break;
            case 16:
                MagSettings.mRes = SENSITIVITY_MAGNETOMETER_16;
                break;
        }
    }
    
    public void sleepGyro(boolean enable)
    {
        if (enable){
            CTRL_REG9Val |= (1<<6);
        }
        else {
            CTRL_REG9Val &= ~(1<<6);
        }
        agWriteByte(CTRL_REG9, CTRL_REG9Val);
    }
    
    public void enableFIFO(boolean enable)
    {
        if (enable) {
            CTRL_REG9Val |= (1<<1);
        }
        else {
            CTRL_REG9Val &= ~(1<<1);
        }
        agWriteByte(CTRL_REG9, CTRL_REG9Val);
    }
//    
//    public void setFIFO(fifoMode_type fifoMode, uint8_t fifoThs)
//    {
//        // Limit threshold - 0x1F (31) is the maximum. If more than that was asked
//        // limit it to the maximum.
//        int threshold = fifoThs <= 0x1F ? fifoThs : 0x1F;
//        agWriteByte(FIFO_CTRL, ((fifoMode & 0x7) << 5) | (threshold & 0x1F));
//    }


    
    
    
    
    private void agWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LSM9DS1_AG_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
    }
    private void mWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LSM9DS1_M_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
    }
    
}
