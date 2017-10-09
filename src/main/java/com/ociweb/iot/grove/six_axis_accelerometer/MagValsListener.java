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
public interface MagValsListener {
    void magneticValues(int x,int y,int z);
}
