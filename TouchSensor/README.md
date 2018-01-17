# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will turn the LED light on whenever the button is pressed down.

Demo code: 


```java
package com.ociweb.grove;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;
import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D4;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class TouchSensor implements FogApp
{
	private static final Port TOUCH_SENSOR_PORT = D4;
	private static final Port LED_PORT = D2;
	
    @Override
    public void declareConnections(Hardware c) {
    	c.connect(TouchSensor, TOUCH_SENSOR_PORT);
    	c.connect(LED, LED_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.addDigitalListener(new TouchSensorBehavior(runtime));
    }
}
```

Behavior class:

```java
package com.ociweb.grove;

import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.FogCommandChannel.*;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class TouchSensorBehavior implements DigitalListener {

	private static final Port LED_PORT = D2;

	final FogCommandChannel channel1;
	public TouchSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
    	channel1 = runtime.newCommandChannel(PIN_WRITER);

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
        channel1.setValueAndBlock(LED_PORT, value == 1, 500);                                                                            //delays a future action

	}

}
```



When executed, the above code will turn the LED light on while the touch sensor is activated by pressing the touch sensor or by having your finger close enough to the sensor. The touch sensor will also work even with a material in between the sensor and your finger, allowing many uses of this sensor. After the light is turned off, there will also be a 500 millisecond delay before the LED light can be turned on again.
The behavior passes a 1 as value when the button is pressed, and 0 when it is released. In order to send a signal to the relay on the digital port, use the setValue() method to check if the value is equivalent to 1, and when it is, a signal will be sent.
