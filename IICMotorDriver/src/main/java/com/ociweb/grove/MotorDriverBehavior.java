/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.motor_driver.MotorDriverTwig.MotorDriver;
import com.ociweb.iot.grove.motor_driver.MotorDriver_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.pronghorn.pipe.BlobReader;

import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */

public class MotorDriverBehavior implements StartupListener, PubSubListener {
    private final FogCommandChannel ch;
    private final MotorDriver_Transducer controller;
    private int channel1Power = 150;
    private int channel2Power = 150;

    public enum Port {
        A,
        B
    }

    public MotorDriverBehavior(FogRuntime runtime) {
        this.ch = runtime.newCommandChannel(); //need long command channel length to send Stepper Run commands (24 bytes per step)
        this.controller = MotorDriver.newTransducer(ch);
    }

    @Override
    public void startup() {
        controller.setPower(channel1Power, channel2Power);
        //set the velocity of both motors
        //to stop the motor, use controller.setVelocity(0,0);
//        controller.StepperRun(250);
//        controller.StepperRun(-250);
    }

    /*
        If the controller ports must be operated independently
        use the behavior to synchronize the state.
        We use getMaxVelocity() to hide the controller's integer range.
        Publishers pass in a normalized -1.0...1.0 value.
     */
    @Override
    public boolean message(CharSequence charSequence, BlobReader blobReader) {
        int idx = blobReader.readInt();
        double value = blobReader.readDouble();
        Port port = MotorDriverBehavior.Port.values()[idx];
        int ranged = (int)(value * controller.getMaxVelocity());
        switch (port) {
            case A:
                if (channel1Power == ranged) return true;
                channel1Power = ranged;
                break;
            case B:
                if (channel2Power == ranged) return true;
                channel2Power = ranged;
                break;
        }
        controller.setPower(channel1Power, channel2Power);
        return true;
    }

}
