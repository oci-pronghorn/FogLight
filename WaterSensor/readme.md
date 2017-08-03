# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project:
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The project turns the Raspberry Pi into a server and serves up readings from the Water Sensor Grove Twig on  <Pi's IP Address>/Water_Sensor.


```java
package com.ociweb.grove;


import static com.ociweb.grove.RestfulWaterSensorConstants.*;
import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;

import com.ociweb.iot.maker.*;

public class IoTApp implements FogApp
{
	private int webRoute = -1;
	@Override
	public void declareConnections(Hardware c) {
		c.connect(WaterSensor, WATER_SENSOR_PORT);

		c.enableServer(serverIsTLS, 
				serverIsLarge,
				hostIP,
				8088);	

		c.enableTelemetry();

		webRoute = c.registerRoute(requestRoute);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		//TODO: Need to update error handling so that if user forgets to include webroute, it's obvious
		runtime.registerListener(new RestfulWaterSensorBehavior(runtime)).includePorts(WATER_SENSOR_PORT);
	}

}
```



Since the IoT app needs to both exist AnalogListener behavior as well as well RestListener behavior, a custom listener (```RestfulWaterSensorBehavior```) that extends both is needed :


```java
package com.ociweb.grove;


import java.io.IOException;

import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;

public class RestfulWaterSensorBehavior implements AnalogListener, RestListener  {
	private int val = -1;
	private FogCommandChannel ch;
	
	public RestfulWaterSensorBehavior(FogRuntime runtime) {
		this.ch = runtime.newCommandChannel(MsgCommandChannel.NET_RESPONDER | MsgCommandChannel.DYNAMIC_MESSAGING); 
	}	

	@Override
	public boolean restRequest(HTTPRequestReader reader) {
		return ch.publishHTTPResponse(reader, 200, HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, HTTPContentTypeDefaults.HTML,
				(writer)->{
					try {
						writer.append(Integer.toString(this.val));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
			System.out.println(val);
			this.val = value;
	}

}
```


The constants needed for both classes can be consolidated in a third .java file:


```java
package com.ociweb.grove;

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

