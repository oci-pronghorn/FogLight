/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.mini_motor_driver;

/**
 *
 * @author huydo
 */
public interface MiniMotorDriverListener {
    void ch1FaultStatus(int ch1Status);
    void ch2FaultStatus(int ch2Status);
}
