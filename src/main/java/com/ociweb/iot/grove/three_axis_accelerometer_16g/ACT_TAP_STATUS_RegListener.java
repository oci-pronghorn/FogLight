/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.three_axis_accelerometer_16g;

/**
 *
 * @author huydo
 */
public interface ACT_TAP_STATUS_RegListener extends ThreeAxisAccelerometer_16gListener{
    void act_tapStatus(int byteRead); //byteRead is the 8 bit stored in ACT_TAP_STATUS register

}
