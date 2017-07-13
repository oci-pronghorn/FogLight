/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import static com.ociweb.iot.grove.motor_driver.MotorDriverTwig.MotorDriver;
import com.ociweb.iot.grove.motor_driver.MotorDriver_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */
public class MotorDriverBehavior implements StartupListener{
    FogCommandChannel ch;
    MotorDriver_Facade controller;
    public MotorDriverBehavior(FogRuntime runtime){    
        this.ch = runtime.newCommandChannel(I2C_WRITER, 52000);
        controller = MotorDriver.newFacade(ch);
    }
    
    @Override
    public void startup() {
        controller.setVelocity(150, 150); //set the velocity of both motors 
        //to stop the motor, use controller.setVelocity(0,0);
//        controller.StepperRun(250);
//        controller.StepperRun(-250);


    }
}
