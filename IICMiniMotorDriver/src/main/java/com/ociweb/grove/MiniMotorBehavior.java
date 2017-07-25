/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriverListener;
import com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriver_Transducer;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.maker.FogCommandChannel.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

/**
 *
 * @author huydo
 */
public class MiniMotorBehavior implements AnalogListener,MiniMotorDriverListener {
    FogCommandChannel ch;
    MiniMotorDriver_Transducer motorController;
    
    MiniMotorBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        motorController = new MiniMotorDriver_Transducer(ch,this);
    }
    
    
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        System.out.println("value: "+value);
        int speed = (value-512)/8;
        
        motorController.setVelocity(1, speed);
        motorController.setVelocity(2, speed);
    }
    
    @Override
    public void ch1FaultStatus(int ch1Status) {
        System.out.println("CH1: "+ch1Status);
    }
    
    @Override
    public void ch2FaultStatus(int ch2Status) {
        System.out.println("CH2: "+ch2Status);
    }
    
}
