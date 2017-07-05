/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.i2c_motor_driver;

/**
 *
 * @author huydo
 */
public class I2CMotorDriver_Constants {
    public static final int MotorSpeedSet             =0x82;
    public static final int PWMFrequenceSet           =0x84;
    public static final int DirectionSet              =0xaa;
    public static final int MotorSetA                 =0xa1;
    public static final int MotorSetB                 =0xa5;
    public static final int Nothing                   =0x01;
    public static final int MOTOR1 			       =   1;
    public static final int MOTOR2 			       =   2;
    
    public static final int BothClockWise             =0x0a;
    public static final int BothAntiClockWise         =0x05;
    public static final int M1CWM2ACW                 =0x06;
    public static final int M1ACWM2CW                 =0x09;
    
    public static final int ClockWise                 =0x0a;
    public static final int AntiClockWise             =0x05;
    public static final int F_31372Hz                 =0x01;
    public static final int F_3921Hz                  =0x02;
    public static final int F_490Hz                   =0x03;
    public static final int F_122Hz                   =0x04;
    public static final int F_30Hz                    =0x05;
}
