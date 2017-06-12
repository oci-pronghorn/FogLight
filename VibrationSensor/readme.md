# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLight-Examples/blob/master/README.md)

# Example project:
```java
import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup
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
	public void declareBehavior(DeviceRuntime runtime) {
		final CommandChannel ch = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
			if (port == VIBRATION_SENSOR_PORT){
				if (value < threshold){
					ch.setValue(BUZZER_PORT,0);
				}
				else {
					ch.setValue(BUZZER_PORT, 1);
				}		
			}
		});
	}
}
```
The lambda passed into ```runtime.addAnalogListener()``` triggers the buzzer if the value of vibration is over a predetermined threshold, which is 800 in this case. 