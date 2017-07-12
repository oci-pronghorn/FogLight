
# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application using the Motion Sensor: whenever the Motion Sensor detects a movement, an LED light will turn on:

Demo code:
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.MotionSensor;
import static com.ociweb.iot.grove.AnalogDigitalTwig.LED;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {
    
    public static final Port LED_PORT = D4;
    public static final Port PIR_SENSOR = D3;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(MotionSensor, PIR_SENSOR);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new MotionSensorBehavior(runtime));
        
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

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import static com.ociweb.grove.IoTApp.*;

/**
 *
 * @author huydo
 */
public class MotionSensorBehavior implements DigitalListener{
    
    private final FogCommandChannel ledChannel;
    
    public MotionSensorBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel();
    }
    
    @Override
    public void digitalEvent(Port port, long time, long durationMillis, int value) {
        ledChannel.setValue(LED_PORT,value==1);
        System.out.println("Stop moving!");
    }
    
}
```



When executed, the above code will cause the LED on D4 (digital output 4) to turn on when the motion sensor on D3 (digital input 3) detects a movement.

In digitalEvent() method of the Behavior class,  ```value``` is set to 1 when the motion sensor detects a movement, and 0 otherwise. In order to turn on the LED on the digital port, we need to use setValue() method to send boolean value to the digital port connected to the LED (a _true_ will turn the LED on, while a _false_ will turn if off).







