# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to measure the illumination density using a UV Sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.UVSensor;
import static com.ociweb.iot.maker.Port.A2;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{

    private static final Port UV_SENSOR_PORT = A2;

    @Override
    public void declareConnections(Hardware c) {

        c.connect(UVSensor, UV_SENSOR_PORT,500);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
      
        runtime.registerListener(new UVSensorBehavior(runtime));
    }
}
```


Then specify the behavior of the program in the Behavior class:

```java
package com.ociweb.grove;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class UVSensorBehavior implements AnalogListener {
    public UVSensorBehavior(FogRuntime runtime) {   
    }
    
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        // TODO Auto-generated method stub
        System.out.println("The Illumination intensity is : "+(value/1023*307)+"mW/m^2");
        
    }
    
}
```






