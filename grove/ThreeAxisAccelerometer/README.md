# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to detect if the accelerometer sensor is in free fall

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 


```java
package com.ocweb.grove;


import static com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 
    @Override
    public void declareConnections(Hardware c) {
        c.enableTelemetry();
        c.connect(ThreeAxisAccelerometer_16g.GetXYZ);
        c.connect(ThreeAxisAccelerometer_16g.GetInterrupt);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {

        runtime.registerListener(new AccelerometerBehavior(runtime));
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
package com.ocweb.grove;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.*;

import com.ociweb.iot.grove.three_axis_accelerometer_16g.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class AccelerometerBehavior implements StartupListener,AccelInterruptListener,AccelValsListener {
    
    private final FogCommandChannel c;
    private final ThreeAxisAccelerometer_16g_Transducer accSensor;
    
    public AccelerometerBehavior(FogRuntime runtime){
        this.c = runtime.newCommandChannel();
        accSensor = ThreeAxisAccelerometer_16g.GetInterrupt.newTransducer(c);
        accSensor.registerListener(this);
    }
    
    @Override
    public void startup() {
        accSensor.setFreeFallDuration(4);
        accSensor.setFreeFallThreshold(9);
        accSensor.enableFreeFallInterrupt();
    }

    @Override
    public void AccelInterruptStatus(int singletap, int doubletap, int activity, int inactivity, int freefall) {
        if(freefall == 1){
            System.out.println("free falling..");
        }
    }

    @Override
    public void accelerationValues(int x, int y, int z) {
        System.out.println("x: "+x);
        System.out.println("y: "+y);
        System.out.println("z: "+z);
    }


}
```


When executed, the program will print out the x,y,z acceleration values and notifies when the device is in free fall.

[1] The ThreeAxisAccelerometer source code supports 3 Listener interfaces:
1. AccelValsListener
This listener has accelVals() abstract method which passes x,y,z acceleration values in units of mg (1000 mg = 9.8 m/s^2)
2. AccelInterruptListener
The AccelInterruptStatus() abstract method has 5 fields corresponding to 5 types of interrupt. The int of the field will be 1 if the corresponding action is detected. 
3. ActTapListener
Each parameter in the abstract methods will be 1 if the corresponding activity/ inactivity on that axis is detected. 

For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/three_axis_accelerometer_16g/ThreeAxisAccelerometer_16g_Transducer.java).



