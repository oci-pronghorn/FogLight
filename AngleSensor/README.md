# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to control two LEDs using an Angle Sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    public static final Port LED1_PORT = D3;
    public static final Port LED2_PORT = D4;
    public static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        
        c.connect(LED, LED1_PORT);
        c.connect(LED,LED2_PORT);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new AngleSensorBehavior(runtime));
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

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import static com.ociweb.grove.IoTApp.*;
import static com.ociweb.iot.maker.FogCommandChannel.*;

/**
 *
 * @author huydo
 */
public class AngleSensorBehavior implements AnalogListener {

    private final FogCommandChannel led1Channel;
    private    final FogCommandChannel led2Channel;
    
    public AngleSensorBehavior(FogRuntime runtime){
        this.led1Channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER);
        this.led2Channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER);
    }
        
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        if(value>512){
                led2Channel.setValue(LED2_PORT,true);
            }else{
                led2Channel.setValue(LED2_PORT,false);
            }
            led1Channel.setValue(LED1_PORT,value/4);
    }
    
}
```


When executed, turning the knob will cause LED2 to turn on/off and LED1 to fade in/out accordingly.




