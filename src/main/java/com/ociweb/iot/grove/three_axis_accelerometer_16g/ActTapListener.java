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
public interface ActTapListener extends ThreeAxisAccelerometer_16gListener{
    void activityStatus(int actX,int actY,int actZ);
    void tapStatus(int tapX,int tapY,int tapZ);
}
