/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.astropi.listeners;

/**
 *
 * @author huydo
 */
public interface TemperatureListener extends AstroPiListener{
    void temperatureVal(double temp);
}
