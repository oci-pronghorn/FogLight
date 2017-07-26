/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.astropi;

/**
 *
 * @author huydo
 */
public interface GyroListener extends AstroPiListener{
    void gyroEvent(double x,double y,double z);
}
