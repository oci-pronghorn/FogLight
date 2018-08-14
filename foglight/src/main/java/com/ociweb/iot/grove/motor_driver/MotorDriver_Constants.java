/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.iot.grove.motor_driver;

/**
 *
 * @author huydo
 */
public class MotorDriver_Constants {
    public static final int SPEED_REG               =0x82;
    public static final int PWM_FREQ_REG            =0x84;
    public static final int DIR_REG                 =0xaa;
    
    public static final int DUMMY_BYTE              =0x01;
    
    public static final int M1CW_M2CW               =0x0a;
    public static final int M1ACW_M2ACW             =0x05;
    public static final int M1CW_M2ACW              =0x06;
    public static final int M1ACW_M2CW              =0x09;

    public static final int F_31372Hz                 =0x01;
    public static final int F_3921Hz                  =0x02;
    public static final int F_490Hz                   =0x03;
    public static final int F_122Hz                   =0x04;
    public static final int F_30Hz                    =0x05;
}
