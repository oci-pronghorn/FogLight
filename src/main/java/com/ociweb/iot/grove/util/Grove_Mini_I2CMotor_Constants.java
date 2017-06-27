/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.util;

/**
 *
 * @author huydo
 */
public class Grove_Mini_I2CMotor_Constants {
    public static final int CH1_ADD =  0xC4>>>1;
    public static final int CH2_ADD =  0xC0>>>1;
    
    public static final int CTL_REG = 0x00;
    public static final int FAULT_REG = 0x01;
    
    public static final int CLEAR = (byte)(0x80 & 0xFF);  
    public static final int STOP  = 0;
    public static final int BRAKE = 0x03;
    
}
