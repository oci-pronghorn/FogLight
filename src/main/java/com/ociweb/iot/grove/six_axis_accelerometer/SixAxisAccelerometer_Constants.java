/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.six_axis_accelerometer;

/**
 *
 * @author huydo
 */
public class SixAxisAccelerometer_Constants {
    /* LSM303 Address definitions */
    public static final int LSM303D_ADDR = 0x1E;  // assuming SA0 grounded
    
    /* LSM303 Register definitions */
    public static final int TEMP_OUT_L		= 0x05;
    public static final int TEMP_OUT_H		= 0x06;
    public static final int	STATUS_REG_M	= 0x07;
    public static final int OUT_X_L_M 		= 0x08;
    public static final int OUT_X_H_M 		= 0x09;
    public static final int OUT_Y_L_M 		= 0x0A;
    public static final int OUT_Y_H_M 		= 0x0B;
    public static final int OUT_Z_L_M 		= 0x0C;
    public static final int OUT_Z_H_M 		= 0x0D;
    public static final int	WHO_AM_I		= 0x0F;
    public static final int	INT_CTRL_M		= 0x12;
    public static final int	INT_SRC_M		= 0x13;
    public static final int	INT_THS_L_M		= 0x14;
    public static final int	INT_THS_H_M		= 0x15;
    public static final int	OFFSET_X_L_M	= 0x16;
    public static final int	OFFSET_X_H_M	= 0x17;
    public static final int	OFFSET_Y_L_M	= 0x18;
    public static final int	OFFSET_Y_H_M	= 0x19;
    public static final int	OFFSET_Z_L_M	= 0x1A;
    public static final int	OFFSET_Z_H_M	= 0x1B;
    public static final int REFERENCE_X 	= 0x1C;
    public static final int REFERENCE_Y 	= 0x1D;
    public static final int REFERENCE_Z 	= 0x1E;
    public static final int CTRL_REG0 		= 0x1F;
    public static final int CTRL_REG1 		= 0x20;
    public static final int CTRL_REG2		= 0x21;
    public static final int CTRL_REG3 		= 0x22;
    public static final int CTRL_REG4 		= 0x23;
    public static final int CTRL_REG5 		= 0x24;
    public static final int CTRL_REG6 		= 0x25;
    public static final int CTRL_REG7 		= 0x26;
    public static final int	STATUS_REG_A	= 0x27;
    public static final int OUT_X_L_A 		= (byte)((0x28 | 0x80)&0xff);
    public static final int OUT_X_H_A 		= (0x29 | 0x80);
    public static final int OUT_Y_L_A 		= (0x2A | 0x80);
    public static final int OUT_Y_H_A 		= (0x2B | 0x80);
    public static final int OUT_Z_L_A 		= (0x2C | 0x80);
    public static final int OUT_Z_H_A 		= (0x2D | 0x80);
    public static final int	FIFO_CTRL		= 0x2E;
    public static final int	FIFO_SRC		= 0x2F;
    public static final int	IG_CFG1			= 0x30;
    public static final int	IG_SRC1			= 0x31;
    public static final int	IG_THS1			= 0x32;
    public static final int	IG_DUR1			= 0x33;
    public static final int	IG_CFG2			= 0x34;
    public static final int	IG_SRC2			= 0x35;
    public static final int	IG_THS2			= 0x36;
    public static final int	IG_DUR2			= 0x37;
    public static final int	CLICK_CFG		= 0x38;
    public static final int	CLICK_SRC		= 0x39;
    public static final int	CLICK_THS		= 0x3A;
    public static final int	TIME_LIMIT		= 0x3B;
    public static final int	TIME_LATENCY	= 0x3C;
    public static final int	TIME_WINDOW		= 0x3D;
    public static final int	ACT_THS			= 0x3E;
    public static final int	ACT_DUR			= 0x3F;
    
    public static final int MAG_SCALE_2 	= 0x00; //full-scale is +/-2Gauss
    public static final int MAG_SCALE_4 	= 0x20; //+/-4Gauss
    public static final int MAG_SCALE_8 	= 0x40; //+/-8Gauss
    public static final int MAG_SCALE_12 	= 0x60; //+/-12Gauss
}
