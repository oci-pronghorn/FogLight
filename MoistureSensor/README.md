
# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The Moisture Sensor will return a ```value``` of 0 when it's dry. Then ```value``` will increase the moister the sensor is up to 1023.

Demo code (copy and paste this to a FogLighter project):

First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {
    
    private static final Port SENSOR_PORT = A2;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(MoistureSensor, SENSOR_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.addAnalogListener(new MoistureSensorBehavior(runtime));   
    }
}```


Then specify the behavior of the program in the Behavior class:


```java
package com.ociweb.grove;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class MoistureSensorBehavior implements AnalogListener {

	public MoistureSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		// TODO Auto-generated method stub
        System.out.println(value);

	}

}
```








