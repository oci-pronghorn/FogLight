# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to control a stepper motor using the Motor Driver.


Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.motor_driver.MotorDriverTwig.MotorDriver;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;

import com.ociweb.iot.maker.Hardware;


public class IoTApp implements FogApp
{
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(MotorDriver);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener(new MotorDriverBehavior(runtime));
    }
}
```


Then specify the behavior of the program in the Behavior class:


```java
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
```


When executed, the stepper motor will turn 250 steps in the forward direction, then turn 250 steps backwards.

For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/motor_driver/MotorDriver_Transducer.java) .



