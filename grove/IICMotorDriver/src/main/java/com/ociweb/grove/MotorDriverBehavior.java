/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.motor_driver.MotorDriverTwig.MotorDriver;
import com.ociweb.iot.grove.motor_driver.MotorDriver_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
/**
 *
 * @author huydo
 */

public class MotorDriverBehavior implements StartupListener {
    private final FogCommandChannel ch;
    private final MotorDriver_Transducer controller;
    private int channel1Power = 150;
    private int channel2Power = 150;

    public MotorDriverBehavior(FogRuntime runtime) {
        this.ch = runtime.newCommandChannel(); //need long command channel length to send Stepper Run commands (24 bytes per step)
        ch.ensureI2CWriting(10000,100);
        this.controller = MotorDriver.newTransducer(ch);
    }

    @Override
    public void startup() {
        controller.setPower(channel1Power, channel2Power);
        //set the velocity of both motors
        //to stop the motor, use controller.setVelocity(0,0);
        controller.StepperRun(250);
        controller.StepperRun(-250);
    }


}
