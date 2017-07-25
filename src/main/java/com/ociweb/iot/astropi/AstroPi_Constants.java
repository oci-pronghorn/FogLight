package com.ociweb.iot.astropi;

public class AstroPi_Constants {
    
    /*___________LED Matrix and JoyStick __________________*/
    public static final int LED_I2C_ADDR = 0x46;
    public static final int LED_REG_ADDR = 0x00;
    
    public static final byte JOYSTICK_REG_ADDR = (byte)0b11110010;
    /*___LSM9DS1 3D accelerometer, 3D gyroscope, 3D magnetometer _________*/
    public static final int LSM9DS1_AG_ADDR = 0x6a;
    public static final int LSM9DS1_M_ADDR = 0x1c;
    
    public static final int ACT_THS		=	0x04;
    public static final int ACT_DUR		=	0x05;
    public static final int INT_GEN_CFG_XL	=0x06;
    public static final int INT_GEN_THS_X_XL=0x07;
    public static final int INT_GEN_THS_Y_XL=0x08;
    public static final int INT_GEN_THS_Z_XL=0x09;
    public static final int INT_GEN_DUR_XL	=0x0A;
    public static final int REFERENCE_G	=	0x0B;
    public static final int INT1_CTRL	=	0x0C;
    public static final int INT2_CTRL	=	0x0D;
    public static final int WHO_AM_I_XG	=	0x0F;
    public static final int CTRL_REG1_G	=	0x10;
    public static final int CTRL_REG2_G	=	0x11;
    public static final int CTRL_REG3_G	=	0x12;
    public static final int ORIENT_CFG_G	=0x13;
    public static final int INT_GEN_SRC_G	=0x14;
    public static final int OUT_TEMP_L	=	0x15;
    public static final int OUT_TEMP_H	=	0x16;
    public static final int STATUS_REG_0	=0x17;
    public static final int OUT_X_L_G	=	0x18;
    public static final int OUT_X_H_G	=	0x19;
    public static final int OUT_Y_L_G	=	0x1A;
    public static final int OUT_Y_H_G	=	0x1B;
    public static final int OUT_Z_L_G	=	0x1C;
    public static final int OUT_Z_H_G	=	0x1D;
    public static final int CTRL_REG4	=	0x1E;
    public static final int CTRL_REG5_XL	=0x1F;
    public static final int CTRL_REG6_XL	=0x20;
    public static final int CTRL_REG7_XL	=0x21;
    public static final int CTRL_REG8	=	0x22;
    public static final int CTRL_REG9	=	0x23;
    public static final int CTRL_REG10	=	0x24;
    public static final int INT_GEN_SRC_XL	=0x26;
    public static final int STATUS_REG_1	=0x27;
    public static final int OUT_X_L_XL	=	0x28;
    public static final int OUT_X_H_XL	=	0x29;
    public static final int OUT_Y_L_XL	=	0x2A;
    public static final int OUT_Y_H_XL	=	0x2B;
    public static final int OUT_Z_L_XL	=	0x2C;
    public static final int OUT_Z_H_XL	=	0x2D;
    public static final int FIFO_CTRL	=	0x2E;
    public static final int FIFO_SRC	=	0x2F;
    public static final int INT_GEN_CFG_G	=0x30;
    public static final int INT_GEN_THS_XH_G=0x31;
    public static final int INT_GEN_THS_XL_G=0x32;
    public static final int INT_GEN_THS_YH_G=0x33;
    public static final int INT_GEN_THS_YL_G=0x34;
    public static final int INT_GEN_THS_ZH_G=0x35;
    public static final int INT_GEN_THS_ZL_G=0x36;
    public static final int INT_GEN_DUR_G	=0x37;
    
    public static final int OFFSET_X_REG_L_M=0x05;
    public static final int OFFSET_X_REG_H_M=0x06;
    public static final int OFFSET_Y_REG_L_M=0x07;
    public static final int OFFSET_Y_REG_H_M=0x08;
    public static final int OFFSET_Z_REG_L_M=0x09;
    public static final int OFFSET_Z_REG_H_M=0x0A;
    public static final int WHO_AM_I_M	=	0x0F;
    public static final int CTRL_REG1_M	=	0x20;
    public static final int CTRL_REG2_M	=	0x21;
    public static final int CTRL_REG3_M	=	0x22;
    public static final int CTRL_REG4_M	=	0x23;
    public static final int CTRL_REG5_M	=	0x24;
    public static final int STATUS_REG_M	=0x27;
    public static final int OUT_X_L_M	=	0x28;
    public static final int OUT_X_H_M	=	0x29;
    public static final int OUT_Y_L_M	=	0x2A;
    public static final int OUT_Y_H_M	=	0x2B;
    public static final int OUT_Z_L_M	=	0x2C;
    public static final int OUT_Z_H_M	=	0x2D;
    public static final int INT_CFG_M	=	0x30;
    public static final int INT_SRC_M	=	0x31;
    public static final int INT_THS_L_M	=	0x32;
    public static final int INT_THS_H_M	=	0x33;
    
    public static final int WHO_AM_I_AG_RSP	=0x68;
    public static final int WHO_AM_I_M_RSP	=0x3D;
    
        // Sensor Sensitivity Constants
// Values set according to the typical specifications provided in
// table 3 of the LSM9DS1 datasheet. (pg 12)
    public static final double SENSITIVITY_ACCELEROMETER_2  =0.000061;
    public static final double SENSITIVITY_ACCELEROMETER_4  =0.000122;
    public static final double SENSITIVITY_ACCELEROMETER_8  =0.000244;
    public static final double SENSITIVITY_ACCELEROMETER_16 =0.000732;
    public static final double SENSITIVITY_GYROSCOPE_245    =0.00875;
    public static final double SENSITIVITY_GYROSCOPE_500    =0.0175;
    public static final double SENSITIVITY_GYROSCOPE_2000   =0.07;
    public static final double SENSITIVITY_MAGNETOMETER_4   =0.00014;
    public static final double SENSITIVITY_MAGNETOMETER_8   =0.00029;
    public static final double SENSITIVITY_MAGNETOMETER_12  =0.00043;
    public static final double SENSITIVITY_MAGNETOMETER_16  =0.00058;
    
    /*___________Humidity Sensor______________________________________*/
    
    public static final int HTS221_ADDRESS   =  0x5F;
    
    public static final int WHO_AM_I_HUM         =  0x0F;
    public static final int WHO_AM_I_RETURN_HUM  =  0xBC; //This read-only register contains the device identifier, set to BCh
    
    public static final int AVERAGE_REG        =0x10;	// To configure humidity/temperature average.
    public static final int AVERAGE_DEFAULT    =0x1B;
    
    /*
    * [7] PD: power down control
    * (0: power-down mode; 1: active mode)
    *
    * [6:3] Reserved
    *
    * [2] BDU: block data update
    * (0: continuous update; 1: output registers not updated until MSB and LSB reading)
    The BDU bit is used to inhibit the output register update between the reading of the upper
    and lower register parts. In default mode (BDU = ?0?), the lower and upper register parts are
    updated continuously. If it is not certain whether the read will be faster than output data rate,
    it is recommended to set the BDU bit to ?1?. In this way, after the reading of the lower (upper)
    register part, the content of that output register is not updated until the upper (lower) part is
    read also.
    *
    * [1:0] ODR1, ODR0: output data rate selection (see table 17)
    */
    public static final int CTRL_REG1_HUM        =0x20;
    public static final int POWER_UP_HUM         =0x80;
    public static final int BDU_SET_HUM          =0x4;
    public static final int ODR0_SET_HUM         =0x1 ; // setting sensor reading period 1Hz
    public static final int CTRL_REG2_HUM        =0x21;
    public static final int CTRL_REG3_HUM        =0x22;
    public static final int REG_DEFAULT_HUM      =0x00;
    public static final int STATUS_REG_HUM       =0x27;
    public static final int TEMPERATURE_READY_HUM=0x1;
    public static final int HUMIDITY_READY   =0x2;
    public static final int HUMIDITY_L_REG   =(byte)((0x28 | 0x80)&0xff);
    public static final int HUMIDITY_H_REG   =0x29;
    public static final int TEMP_L_REG_HUM       =(byte)((0x2A | 0x80)&0xff);
    public static final int TEMP_H_REG_HUM       =0x2B;
    /*
    * calibration registry should be read for temperature and humidity calculation.
    * Before the first calculation of temperature and humidity,
    * the master reads out the calibration coefficients.
    * will do at init phase
    */
    public static final int CALIB_START        =(byte)((0x30 | 0x80)&0xff);
    public static final int CALIB_END	   =    0x3F;
    
    
    /*___________Pressure Sensor________________________*/
    
    public static final int LPS25H_ADDRESS =    0x5C;

public static final int WHO_AM_I_P         =0x0F;
public static final int WHO_AM_I_RETURN_P =0xBD;// Contains the device ID, BDh
public static final int RES_CONF_REG     =0x10;// Pressure and Temperature internal average configuration.
public static final int RES_CONF_DEFAULT =0x05;


/*
 * [7] PD: power down control.
 * Default value: 0
 * (0: power-down mode; 1: active mode)
 *
 * [6:4] ODR2, ODR1, ODR0: output data rate selection.
 * Default value: 00
 *
 * [3] DIFF_EN: Interrupt circuit enable.
 * Default value: 0
 * (0: interrupt generation disabled; 1: interrupt circuit enabled)
 *
 * [2] BDU: block data update.
 * Default value: 0
 * (0: continuous update; 1: output registers not updated until MSB and LSB reading)
 BDU bit is used to inhibit the output registers update between the reading of upper and
 lower register parts. In default mode (BDU = ?0?), the lower and upper register parts are
 updated continuously. If it is not sure to read faster than output data rate, it is recommended
 to set BDU bit to ?1?. In this way, after the reading of the lower (upper) register part, the
 content of that output registers is not updated until the upper (lower) part
 *
 * [1] RESET_AZ: Reset AutoZero function. Reset REF_P reg, set pressure to default value in RPDS
 * register (@0x39/A)
 * (1: Reset. 0: disable)
 *
 * [0] SIM: SPI Serial Interface Mode selection.
 * Default value: 0
 * (0: 4-wire interface; 1: 3-wire interface)
 */
public static final int CTRL_REG1_P  =0x20;
public static final int POWER_UP_P   =0x80;
public static final int BDU_SET_P    =0x04;
public static final int ODR0_SET_P   =0x10; // 1 read each second



public static final int CTRL_REG2_P  =0x21;
public static final int CTRL_REG3_P  =0x22;
public static final int REG_DEFAULT_P=0x00;

/*
 * This register is updated every ODR cycle, regardless of BDU value in CTRL_REG1.
 *
 * P_DA is set to 1 whenever a new pressure sample is available.
 * P_DA is cleared when PRESS_OUT_H (2Ah) register is read.
 *
 * T_DA is set to 1 whenever a new temperature sample is available.
 * T_DA is cleared when TEMP_OUT_H (2Ch) register is read.
 *
 * P_OR bit is set to '1' whenever new pressure data is available and P_DA was set in
 * the previous ODR cycle and not cleared.
 * P_OR is cleared when PRESS_OUT_H (2Ah) register is read.
 *
 * T_OR is set to ?1? whenever new temperature data is available and T_DA was set in
 * the previous ODR cycle and not cleared.
 * T_OR is cleared when TEMP_OUT_H (2Ch) register is read.
 *
 * [7:6] Reserved
 *
 * [5] P_OR: Pressure data overrun. Default value: 0
 * (0: no overrun has occurred;
 * 1: new data for pressure has overwritten the previous one)
 *
 * [4] T_OR: Temperature data overrun. Default value: 0
 * (0: no overrun has occurred;
 * 1: a new data for temperature has overwritten the previous one)
 *
 * [3:2] Reserved
 *
 * [1] P_DA: Pressure data available. Default value: 0
 * (0: new data for pressure is not yet available;
 * 1: new data for pressure is available)
 *
 * [0] T_DA: Temperature data available. Default value: 0
 * (0: new data for temperature is not yet available;
 * 1: new data for temperature is available)
 */
public static final int STATUS_REG_P        =0x27;
public static final int TEMPERATURE_READY_P = 0x1;
public static final int PRESSURE_READY    = 0x2;
public static final int PRESSURE_XL_REG   =(byte)((0x28 | 0x80)&0xff);
public static final int PRESSURE_L_REG    =0x29;
public static final int PRESSURE_H_REG    =0x2A;
public static final int TEMP_L_REG_P        =(byte)((0x2B | 0x80)&0xff);
public static final int TEMP_H_REG_P        =0x2C;
 
}
