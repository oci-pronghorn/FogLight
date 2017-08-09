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
public interface AccelValsListener extends SixAxisAccelerometer_16gListener{
    void accelerationValues(int x,int y,int z);
}
