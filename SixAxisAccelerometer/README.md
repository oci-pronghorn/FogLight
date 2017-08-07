# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to measure the x,y,z acceleration values and find the magnetic North direction

Demo code (copy and paste this to a FogLighter project):
First declare the connections in SixAxisAccelerometer.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods.


```java
package com.ociweb.grove;


import com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometerTwig;
import com.ociweb.iot.maker.*;

public class SixAxisAccelerometer implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {

        c.connect(SixAxisAccelerometerTwig.SixAxisAccelerometer.readAccel);
        c.connect(SixAxisAccelerometerTwig.SixAxisAccelerometer.readMag);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener(new AccelBehavior(runtime));
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
import com.ociweb.iot.grove.six_axis_accelerometer.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */
public class AccelBehavior implements AccelValsListener,StartupListener,MagValsListener { 
    private final FogCommandChannel ch;
    
    private final SixAxisAccelerometer_Transducer accSensor;
    
    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);     
        accSensor = new SixAxisAccelerometer_Transducer(ch,this);
    }
   
    @Override
    public void startup() {
        accSensor.setAccelScale(6);
        accSensor.setMagScale(8);
    }

    @Override
    public void accelVals(int x, int y, int z) {
        System.out.println("x: "+x);
        System.out.println("y: "+y);
        System.out.println("z: "+z);
    }

    @Override
    public void magVals(int x, int y, int z) {
        double heading = 180*Math.atan2(y, x)/3.14;
        heading = (heading<0)?(heading+360):heading;
        System.out.println("heading: "+heading);
    }
    
}
```


When executed, the program will print out the x,y,z acceleration values and the heading direction relative to the magnetic North in degrees.

[1] The SixAxisAccelerometer source code supports 2 Listener interfaces:
1. AccelValsListener
This listener has accelVals() abstract method which passes x,y,z acceleration values. In order to convert the values to units of g (1g = 9.8 m/s^2), divide the accel scale (2,4,6,8 or 16) by 65535 and multiply it by the x,y,z returned value from the listener.

2. MagValsListener
This listener has magVals() abstract method which passes x,y,z magnetic values. In order to convert the values to units of Gauss, divide the magnetic scale (2,4,8 or 12) by 65535 and multiply it by the x,y,z returned value from the listener.


For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/six_axis_accelerometer/SixAxisAccelerometer_Transducer.java).



