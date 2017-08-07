# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to control the velocity of two motors using an Angle Sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods.


```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.grove.mini_motor_driver.MiniMotorDriverTwig.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(MiniMotorDriver.checkFaultCH1);
        c.connect(MiniMotorDriver.checkFaultCH2);
        c.connect(AngleSensor,ANGLE_SENSOR);
    }
    
    @Override
    public void declareBehavior(FogRuntime g) {
        g.registerListener(new MiniMotorBehavior(g));
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
```


The motors are controlled by the setVelocity() method. Turning the knob on the angle sensor will drive the motors forward/ backward with the adjustable speed.

By implementing the MiniMotorDriverListener interface, the fault status of each channel can be checked. ch1Status/ch2Status returns a 1 when there's fault on the corresponding channel.

For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/mini_motor_driver/MiniMotorDriver_Transducer.java).



