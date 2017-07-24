package com.ociweb.iot.astropi;

public class AstroPi_Constants {
    
    //LED Matrix and JoyStick
    public static final int LED_I2C_ADDR = 0x46;
    public static final int LED_REG_ADDR = 0x00;
    
    public static final byte JOYSTICK_REG_ADDR = (byte)0b11110010;
    //LSM9DS1 3D accelerometer, 3D gyroscope, 3D magnetometer
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

}
