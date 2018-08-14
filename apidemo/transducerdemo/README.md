# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.


# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:


Main class:


```java
package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.LightSensor;
import static com.ociweb.iot.maker.Port.A1;

public class TransducerDemo implements FogApp
{
   
	private static final Port LIGHT_SENSOR_PORT = A1;


    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
        c.setTimerPulseRate(2000);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.registerListener(new CustomSumTransducerBehavior(runtime));
    }
          
}
```


Transducer class:


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.transducer.AnalogListenerTransducer;

public class CustomSumTransducer implements AnalogListenerTransducer {
	private final FogRuntime runtime;
	private int sum = 0;
	private int counter = 0;

	CustomSumTransducer(FogRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		
		sum +=value;
		System.out.println("Current sum: " + sum);
		counter++;
		if (counter == 5) {
			runtime.shutdownRuntime();
		}
	}

}
```


Behavior class:


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.maker.FogRuntime;

public class CustomSumTransducerBehavior implements Behavior {
	CustomSumTransducer cst;

	public CustomSumTransducerBehavior(FogRuntime runtime) {
		cst = new CustomSumTransducer(runtime);
	}
}
```



