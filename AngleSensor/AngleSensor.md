# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to control two LEDs using an Angle Sensor.

Demo code (copy and paste this to a FogLighter project):

```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port LED1_PORT = D3;
    private static final Port LED2_PORT = D4;
    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {
        
        c.connect(LED, LED1_PORT);
        c.connect(LED,LED2_PORT);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        final FogCommandChannel led1Channel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        final FogCommandChannel led2Channel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        
        runtime.addAnalogListener((port, time, durationMillis, average, value)->{
            if(value>512){
                led2Channel.setValue(LED2_PORT,true);
            }else{
                led2Channel.setValue(LED2_PORT,false);
            }
            led1Channel.setValue(LED1_PORT,value/4);
        }).includePorts(ANGLE_SENSOR);   
    }
}
```


When executed, turning the knob will cause LED2 to turn on/off and LED1 to fade in/out accordingly.




