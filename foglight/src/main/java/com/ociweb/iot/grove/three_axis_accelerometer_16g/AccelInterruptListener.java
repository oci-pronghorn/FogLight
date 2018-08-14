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
public interface AccelInterruptListener extends ThreeAxisAccelerometer_16gListener {

    void AccelInterruptStatus(int singletap,int doubletap,int activity,int inactivity,int freefall);
}
