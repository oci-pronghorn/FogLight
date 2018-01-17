# What you will need before you start:
- [**Java 8**](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

- [**Maven**](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

- [**Git**](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.
# Hardware Needed
- [**Raspberry Pi**](https://www.raspberrypi.org/)
- [**GrovePi+ Board**](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting your Maven project: 
[Starting a FogLight project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
Demo code:

```java
package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;
import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
	private static final Port VIBRATION_SENSOR_PORT = A0;
	private static final Port BUZZER_PORT = D2;
	
	@Override
	public void declareConnections(Hardware c) {
		c.connect(Buzzer, BUZZER_PORT);
		c.connect(VibrationSensor, VIBRATION_SENSOR_PORT);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
				
		runtime.addAnalogListener(new VibrationSensorBehavior(runtime)).includePorts(VIBRATION_SENSOR_PORT);
	}
}
```

Behavior class:

```java
package com.ociweb.grove;
import static com.ociweb.iot.maker.FogCommandChannel.*;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class VibrationSensorBehavior implements AnalogListener {
	private static final int threshold = 800;
	private static final Port BUZZER_PORT = D2;

	final FogCommandChannel ch;

	public VibrationSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
		ch = runtime.newCommandChannel(PIN_WRITER);
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		// TODO Auto-generated method stub

		if (value < threshold){
			ch.setValue(BUZZER_PORT,false);
		}
		else {
			ch.setValueAndBlock(BUZZER_PORT, true,100);//set the buzzer_port high for at least 100ms
		}
	}

}
```


The behavior class triggers the buzzer if the value of vibration is over a predetermined threshold, which is 800 in this case. 
