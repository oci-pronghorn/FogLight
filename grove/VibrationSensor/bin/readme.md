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
```java
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
	private static final Port VIBRATION_SENSOR_PORT = A0;
	private static final Port BUZZER_PORT = D2;
	private static final int threshold = 800;
	
	@Override
	public void declareConnections(Hardware c) {
		c.connect(Buzzer, BUZZER_PORT);
		c.connect(VibrationSensor, VIBRATION_SENSOR_PORT);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel();
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
				if (value < threshold){
					ch.setValue(BUZZER_PORT,false);
				}
				else {
					ch.setValueAndBlock(BUZZER_PORT, true,100);//set the buzzer_port high for at least 100ms
				}		
			
		}).includePorts(VIBRATION_SENSOR_PORT);
	}
}
```
The lambda passed into ```runtime.addAnalogListener()``` triggers the buzzer if the value of vibration is over a predetermined threshold, which is 800 in this case. 
