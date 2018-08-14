# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The project turns the Raspberry Pi into a server and serves up readings from the Water Sensor Grove Twig on  <Pi's IP Address>/Water_Sensor.

```java
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.maker.*;

public class IoTApp implements IoTSetup
{
	private boolean isReportingToWeb = true;
	private int webRoute = -1;


	
	@Override
	public void declareConnections(Hardware c) {
		c.connect(WaterSensor,RestfulWaterSensorConstants.WATER_SENSOR_PORT);

		if (isReportingToWeb){
			c.enableServer(RestfulWaterSensorConstants.serverIsTLS, 
							RestfulWaterSensorConstants.serverIsLarge,
							RestfulWaterSensorConstants.hostIP,
							8088);	
			
			c.enableTelemetry(true);
			
			webRoute = c.registerRoute(RestfulWaterSensorConstants.requestRoute);
		}
	}


	@Override
	public void declareBehavior(DeviceRuntime runtime) {
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.addRestListener(new RestfulWaterSensorBehavior(runtime), webRoute);
	}

}

```

Since the IoT app needs to both exhist AnalogListener behavior as well as well RestListener behavior, a custom listener (```RestfulWaterSensorBehavior```) that extends both is needed :

```java
import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private CommandChannel ch;
	
	public RestfulWaterSensorBehavior(DeviceRuntime runtime) {
		this.ch = runtime.newCommandChannel(GreenCommandChannel.NET_RESPONDER | GreenCommandChannel.DYNAMIC_MESSAGING); 
	}	

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		
		return ch.publishHTTPResponse(reader, 200, HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, HTTPContentTypeDefaults.HTML,
				(writer)->{
					writer.writeInt(this.val);
				});
		
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		if (port == RestfulWaterSensorConstants.WATER_SENSOR_PORT){
			this.val = value;
		}
	}

}
```

The constants needed for both classes can be consolidated in a third .java file:
```java
import static com.ociweb.iot.maker.Port.A1;

import com.ociweb.iot.maker.Port;

public class RestfulWaterSensorConstants {
	protected static final Port WATER_SENSOR_PORT = A1;
	public static final boolean serverIsTLS = false;
	protected static final boolean serverIsLarge = false;
	protected static final String hostIP = "127.0.0.1"; //using localhost as server	
	protected static final String requestRoute = "/water_sensor";
	
	
}
```
