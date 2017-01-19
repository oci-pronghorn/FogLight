package com.ociweb.iot.astropi;

public class AstroPi_Constants {

	//LSM9DS1 http://www.st.com/content/ccc/resource/technical/document/datasheet/1e/3f/2a/d6/25/eb/48/46/DM00103319.pdf/files/DM00103319.pdf/jcr:content/translations/en.DM00103319.pdf
	//Accelerometer and gyroscope
	private static final byte ACT_THS = 0x04;
	private static final byte ACT_DUR = 0x05;
	private static final byte INT_GEN_CFG_XL = 0x06;
	private static final byte INT_GEN_THS_X_XL = 0x07;
	private static final byte INT_GEN_THS_Y_XL = 0x08;
	private static final byte INT_GEN_THS_Z_ZL = 0x09;
	private static final byte INT_GEN_DUR_XL = 0x0A;
	private static final byte REFERENCE_G = 0x0B;
	private static final byte INT1_CTRL = 0x0C;
	private static final byte INT2_CTRL	 = 0x0D;
	private static final byte WHO_AM_I = 0x0F	;
	private static final byte CTRL_REG1_G	 = 0x10;
	private static final byte CTRL_REG2_G	 = 0x11;
	private static final byte CTRL_REG3_G	 = 0x12;
	private static final byte ORIENT_CFG_G = 0x13;
	private static final byte INT_GEN_SRC_G = 0x14;
	private static final byte OUT_TEMP_L = 0x15;
	private static final byte OUT_TEMP_H = 0x16;
	private static final byte STATUS_REG1 = 0x17;
	private static final byte OUT_X_L_G = 0x18;
	private static final byte OUT_X_H_G = 0x19;
	private static final byte OUT_Y_L_G = 0x1A;
	private static final byte OUT_Y_H_G = 0x1B;
	private static final byte OUT_Z_L_G = 0x1C;
	private static final byte OUT_Z_H_G = 0x1D;
	private static final byte CTRL_REG4 = 0x1E;
	private static final byte CTRL_REG5_XL = 0x1F;
	private static final byte CTRL_REG6_XL = 0x20;
	private static final byte CTRL_REG7_XL = 0x21;
	private static final byte CTRL_REG8 = 0x22;
	private static final byte CTRL_REG9 = 0x23;
	private static final byte CTRL_REG10 = 0x24;
	private static final byte INT_GEN_SRC_XL = 0x26;
	private static final byte STATUS_REG2 = 0x27;
	private static final byte OUT_X_L_XL = 0x28;
	private static final byte OUT_X_H_XL = 0x29;
	private static final byte OUT_Y_L_XL = 0x2A;
	private static final byte OUT_Y_H_XL = 0x2B;
	private static final byte OUT_Z_L_XL = 0x2C;
	private static final byte OUT_Z_H_XL = 0x2D;
	private static final byte FIFO_CTRL = 0x2E;
	private static final byte FIFO_SRC = 0x2F	;
	private static final byte INT_GEN_CFG_G = 0x30;
	private static final byte INT_GEN_THS_XH_G = 0x31;
	private static final byte INT_GEN_THS_XL_G = 0x32;
	private static final byte INT_GEN_THS_YH_G = 0x33;
	private static final byte INT_GEN_THS_YL_G = 0x34;
	private static final byte INT_GEN_THS_ZH_G = 0x35;
	private static final byte INT_GEN_THS_ZL_G = 0x36;
	private static final byte INT_GEN_DUR_G = 0x37;
	
	//Magnetometer constants
	private static final byte OFFSET_X_REG_L_M = 0x05;
	private static final byte OFFSET_X_REG_H_M = 0x06;
	private static final byte OFFSET_Y_REG_L_M = 0x07;
	private static final byte OFFSET_Y_REG_H_M = 0x08;
	private static final byte OFFSET_Z_REG_L_M = 0x09;
	private static final byte OFFSET_Z_REG_H_M = 0x0A;
	private static final byte WHO_AM_I_M = 0x0F;
	private static final byte CTRL_REG1_M = 0x20;
	private static final byte CTRL_REG2_M	= 0x21;
	private static final byte CTRL_REG3_M = 0x22;
	private static final byte CTRL_REG4_M = 0x23;
	private static final byte CTRL_REG5_M = 0x24;
	private static final byte STATUS_REG_M = 0x27;
	private static final byte OUT_X_L_M = 0x28;
	private static final byte OUT_X_H_M = 0x29;
	private static final byte OUT_Y_L_M = 0x2A;
	private static final byte OUT_Y_H_M = 0x2B;
	private static final byte OUT_Z_L_M = 0x2C;
	private static final byte OUT_Z_H_M = 0x2D;
	private static final byte INT_CFG_M = 0x30;
	private static final byte INT_SRC_M = 0x31;
	private static final byte INT_THS_L_M = 0x32;
	private static final byte INT_THS_H_M = 0x33;
	
}
