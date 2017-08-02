/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.grove.motor_driver.MotorDriver_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */
public class MotorDriverBehavior implements StartupListener{
    FogCommandChannel ch;
    MotorDriver_Transducer controller;
    public MotorDriverBehavior(FogRuntime runtime){    
        this.ch = runtime.newCommandChannel(I2C_WRITER, 52000);
        controller = new MotorDriver_Transducer(ch);
    }
    
    @Override
    public void startup() {
        controller.StepperRun(250);
        controller.StepperRun(-250);

    }
}
