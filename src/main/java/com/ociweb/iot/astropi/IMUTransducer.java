/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.astropi;

import com.ociweb.gl.api.facade.StartupListenerTransducer;
import com.ociweb.iot.astropi.listeners.MagListener;
import com.ociweb.iot.astropi.listeners.AccelListener;
import com.ociweb.iot.astropi.listeners.GyroListener;
import com.ociweb.iot.astropi.listeners.AstroPiListener;
import static com.ociweb.iot.astropi.AstroPi_Constants.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.transducer.I2CListenerTransducer;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 *
 * @author huydo
 */
public class IMUTransducer implements IODeviceTransducer,I2CListenerTransducer,StartupListenerTransducer{
    FogCommandChannel target;
    
    public IMUTransducer(FogCommandChannel ch,AstroPiListener... l){
        this.target = ch;
        target.ensureI2CWriting(5000, 50);
        for(AstroPiListener item:l){
            if(item instanceof GyroListener){
                this.gyroListener = (GyroListener) item;
            }
            if(item instanceof AccelListener){
                this.accelListener = (AccelListener) item;
            }
            if(item instanceof MagListener){
                this.magListener = (MagListener) item;
            }
        
        }
    }

    @Override
    public void startup() {
        this.begin(true, true, true);
    }
    
    /**
     * Begin the IMU with the following settings:
     * Gyroscope, Accelerometer and Magnetometer are enabled, with all x,y,z enabled
     * Gyroscope: scale = 245, sampleRate = 119 Hz
     * Accelerometer: scale = 2, sampleRate = 952 Hz
     * Magnetometer: scale = 4, sampleRate = 80 Hz
     * @param gyro either true or false to enable/disable gyroscope
     * @param accel either true or false to enable/disable accelerometer
     * @param mag either true or false to enable/disable magnetometer
     */
    public void begin(boolean gyro,boolean accel,boolean mag){
        constrainScales();
        // Once we have the scale values, we can calculate the resolution
	// of each sensor. That's what these functions are for. One for each sensor
	calcgRes(); // Calculate DPS / ADC tick, stored in gRes variable
	calcmRes(); // Calculate Gs / ADC tick, stored in mRes variable
	calcaRes(); // Calculate g / ADC tick, stored in aRes variable

        GyroSettings.enabled = gyro;
        AccelSettings.enabled = accel;
        MagSettings.enabled = mag;
        if(!mag){
            MagSettings.operatingMode = 2; //power down the magnetometer
        }        
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
    private void initGyro(){
        GyroSettings.CTRL_REG1_GVal = 0;
        // CTRL_REG1_G (Default value: 0x00)
	// [ODR_G2][ODR_G1][ODR_G0][FS_G1][FS_G0][0][BW_G1][BW_G0]
	// ODR_G[2:0] - Output data rate selection
	// FS_G[1:0] - Gyroscope full-scale selection
	// BW_G[1:0] - Gyroscope bandwidth selection
	
	// To disable gyro, set sample rate bits to 0. We'll only set sample
	// rate if the gyro is enabled.
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
                // Otherwise we'll set it to 245 dps (0x0 << 4)
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
                
        
    }
        
    private void initAccel(){
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
        
    }
    
    private void initMag()
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
    }
    /**
     * Convert raw data
     * @param gyro the 16-bit raw gyro data
     * @return converted gyro data in DPS
     */
    private double calcGyro(int gyro)
    {
        // Return the gyro raw reading times our pre-calculated DPS / (ADC tick):
        return GyroSettings.gRes * gyro;
    }
    /**
     * Convert raw data to accelerometer values in g
     * @param accel the 16-bit raw data
     * @return the converted accelerometer values in g
     */
    private double calcAccel(int accel)
    {
        // Return the accel raw reading times our pre-calculated g's / (ADC tick):
        return AccelSettings.aRes * accel;
    }
    /**
     * Convert raw data to magnetic values in Gauss
     * @param mag the 16-bit raw data
     * @return the converted magnetic data in Gauss
     */
    private double calcMag(int mag)
    {
        // Return the mag raw reading times our pre-calculated Gs / (ADC tick):
        return MagSettings.mRes * mag;
    }
    
    /**
     * Set the gyroscope scale to be +/- 245,500,2000 dps
     * @param gScl the scale can be 245,500 or 2000 dps
     */
    public void setGyroScale(int gScl){
        GyroSettings.scale = gScl;        
        calcgRes();
    }
    /**
     * Set the accelerometer scale to be +/- 2,4,8 or 16 g
     * @param aScl scale can be 2,4,8 or 16
     */
    public void setAccelScale(int aScl)
    {
        AccelSettings.scale = aScl;
        // Then calculate a new aRes, which relies on aScale being set correctly:
        calcaRes();
    }
    /**
     * Set the magnetometer scale to be +/- 4,8,12 or 16 Gauss
     * @param mScl scale can be 4,8,12 or 16
     */
    public void setMagScale(int mScl)
    {
        MagSettings.scale = mScl;     
        // Calculate a new mRes, which relies on mScale being set correctly:
        calcmRes();
    }
    /**
     * [sampleRate] sets the output data rate (ODR) of the gyro
     * sampleRate can be set between 1-6
     * 1 = 14.9    4 = 238
     * 2 = 59.5    5 = 476
     * 3 = 119     6 = 952
     * @param gRate int between 1 and 6
     */
    public void setGyroODR(int gRate){
        if((gRate & 0x07) != 0){
           GyroSettings.sampleRate = gRate & 0x07;
        }
    }
    /**
     *  accelerometer sample rate can be 1-6
     *	 1 = 10 Hz    4 = 238 Hz
     *	 2 = 50 Hz    5 = 476 Hz
     *	 3 = 119 Hz   6 = 952 Hz
     * @param aRate int between 1 and 6
     */
    public void setAccelODR(int aRate)
    {
        // Only do this if aRate is not 0 (which would disable the accel)
        if ((aRate & 0x07) != 0)
        {
            AccelSettings.sampleRate = aRate & 0x07;
        }
    }   
    /**
     *   mag data rate can be 0-7
     * 0 = 0.625 Hz  4 = 10 Hz
     * 1 = 1.25 Hz   5 = 20 Hz
     * 2 = 2.5 Hz    6 = 40 Hz
     * 3 = 5 Hz      7 = 80 Hz
     * @param mRate int between 0 and 7
     */
    public void setMagODR(int mRate)
    {
        MagSettings.sampleRate = mRate & 0x07;
    }   
    private void calcgRes()
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
    private void calcaRes()
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
    private void calcmRes()
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

    private void setMagOffset(int axis,int offset){
        int msb,lsb;
        msb = (offset & 0xff00)>>8;
        lsb = (offset & 0xff);
        mWriteByte(AstroPi_Constants.OFFSET_X_REG_L_M +(2*axis),lsb);
        mWriteByte(AstroPi_Constants.OFFSET_X_REG_H_M +(2*axis),msb);
    }

    private void agWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LSM9DS1_AG_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }

    private void mWriteByte(int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(AstroPi_Constants.LSM9DS1_M_ADDR);
        
        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);
        
        target.i2cCommandClose();
        target.i2cFlushBatch();
    }
    
    /**
     * Convert the 6 bytes of X,Y,Z values to the correct two's complement representation
     * @param backing array containing 6 bytes
     * @param position index of the first byte
     * @param length length of the array
     * @param mask
     * @return array of 3 X,Y,Z values ,where array[0] = X, array[1] = Y
     */
    private int[] interpretData(byte[] backing, int position, int length, int mask){
        int[] temp = {0,0,0};
        //format the data from the circular buffer backing[]
        
        temp[0] = (int)(((backing[(position+1)&mask]&0xFF) << 8) | (backing[position&mask]&0xFF));
        temp[1] = (int)(((backing[(position+3)&mask]&0xFF) << 8) | (backing[(position+2)&mask]&0xFF));
        temp[2] = (int)(((backing[(position+5)&mask]&0xFF) << 8) | (backing[(position+4)&mask]&0xFF));
        
        if (temp[0] >= 32768) temp[0] -= 2 * 32768;
        if (temp[1] >= 32768) temp[1] -= 2 * 32768;
        if (temp[2] >= 32768) temp[2] -= 2 * 32768;
        
        return temp;
    }
    
    private int calibrateGyro = 0;
    private int calibrateAccel = 0;
    private int calibrateMag = 0;
    
    private int[] gBiasRawTemp = new int[3];
    private int[] aBiasRawTemp = new int[3];
    int[] magMin ={0,0,0};
    int[] magMax ={0,0,0};
    
    private GyroListener gyroListener;
    private AccelListener accelListener;
    private MagListener magListener;
    
    //Calibrate the sensor by taking 8 samples (ignoring the first sample) and average them. Then set
    // it to be the Bias value
    
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == AstroPi_Constants.LSM9DS1_AG_ADDR){
            if(register == AstroPi_Constants.OUT_X_L_G){
                if(calibrateGyro < 9){
                    if(calibrateGyro != 0){
                        int[] temp = this.interpretData(backing, position, length, mask);
                        gBiasRawTemp[0] += temp[0];
                        gBiasRawTemp[1] += temp[1];
                        gBiasRawTemp[2] += temp[2];
                    }
                    calibrateGyro++;
                }
                else if(calibrateGyro == 9){
                    for(int i=0;i<3;i++){
                        GyroSettings.gBiasRaw[i] = gBiasRawTemp[i]>>3;
                        GyroSettings.gBias[i] = calcGyro(GyroSettings.gBiasRaw[i]);
                    }
                    System.out.println("Gyroscope Calibration Complete.");
                    calibrateGyro++;
                }else{
                    int[] temp = this.interpretData(backing, position, length, mask);
                    gyroListener.gyroEvent(calcGyro(temp[0]-GyroSettings.gBiasRaw[0]), calcGyro(temp[1]-GyroSettings.gBiasRaw[1]), calcGyro(temp[2]-GyroSettings.gBiasRaw[2]));
                }
            }
            if(register == AstroPi_Constants.OUT_X_L_XL){
                if(calibrateAccel < 9){
                    if(calibrateAccel !=0){
                        int[] temp = this.interpretData(backing, position, length, mask);
                        aBiasRawTemp[0] += temp[0];
                        aBiasRawTemp[1] += temp[1];
                        aBiasRawTemp[2] += temp[2] - (int)(1/AccelSettings.aRes);
                    }
                    calibrateAccel++;
                }
                else if(calibrateAccel == 9){
                    for(int i=0;i<3;i++){
                        AccelSettings.aBiasRaw[i] = aBiasRawTemp[i]>>3;
                        AccelSettings.aBias[i] = calcAccel(AccelSettings.aBiasRaw[i]);
                    }
                    System.out.println("Accelerometer Calibration Complete.");
                    calibrateAccel++;
                }else{
                    int[] temp = this.interpretData(backing, position, length, mask);
                    accelListener.accelEvent(calcAccel(temp[0]-AccelSettings.aBiasRaw[0]), calcAccel(temp[1]-AccelSettings.aBiasRaw[1]), calcAccel(temp[2]-AccelSettings.aBiasRaw[2]));
                }
            }
        }
        if(addr == AstroPi_Constants.LSM9DS1_M_ADDR){
            if(register == AstroPi_Constants.OUT_X_L_M){
                if(calibrateMag < 9){
                    if(calibrateMag != 0){
                        int[] temp = this.interpretData(backing, position, length, mask);
                        for (int j = 0; j < 3; j++){
                            if (temp[j] > magMax[j]) magMax[j] = temp[j];
                            if (temp[j] < magMin[j]) magMin[j] = temp[j];
                        }
                    }
                    calibrateMag++;
                }
                else if(calibrateMag == 9){
                    for(int i=0;i<3;i++){
                        MagSettings.mBiasRaw[i] = (magMax[i]+magMin[i])/2;
                        MagSettings.mBias[i] = calcMag(MagSettings.mBiasRaw[i]);
                        setMagOffset(i,MagSettings.mBiasRaw[i]);
                    }
                    calibrateMag++;
                    System.out.println("Magnetometer Calibration Complete.");
                }else{
                    int[] temp = this.interpretData(backing, position, length, mask);
                    magListener.magEvent(calcMag(temp[0]), calcMag(temp[1]),calcMag(temp[2]));
                }
            }
        }
    }

}
