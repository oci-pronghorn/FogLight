# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.


# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using an Analog Transducer.

Demo code:


```java
package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig;

public class AnalogTransducerDemo implements FogApp
{
	private final Port sensorPort = A0;

    @Override
    public void declareConnections(Hardware c) {
      c.connect(AngleSensor, sensorPort);
    
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.registerListener(new AnalogTransducerDemoBehavior(runtime, sensorPort)).includePorts(sensorPort);
    }
}
```


Behavior class:


```java
package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.simple_analog.MovingAverageListener;
import com.ociweb.iot.grove.simple_analog.SimpleAnalogTransducer;
import com.ociweb.iot.grove.simple_analog.SimpleAnalogListener;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class AnalogTransducerDemoBehavior implements  AnalogListener{ //SimpleAnalogListener, MovingAverageListener,
	private final int bucketSize = 15;
	//private final SimpleAnalogTransducer sensor;
	public AnalogTransducerDemoBehavior(FogRuntime rt, Port p){
		//sensor = new SimpleAnalogTransducer(p, this); //no command channel needed because reading
		//sensor.registerListener(this,bucketSize); //this is also the implementation of MovingAverageListener
	//	rt.registerListener(sensor);
	}
	/*
	@Override
	public void movingAverage(double ma) {
		System.out.println("Moving Average: " + ma);
	}

	@Override
	public void simpleAnalogEvent(Port port, long time, long durationMillis, int value) {
		System.out.println("Value:" +value);
	}
*/
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		System.out.println("Analog Event: "+ value);
	}
	//@Override
	public void timeEvent(long arg0, int arg1) {
		System.out.println(arg0);
	}


}
```


This class is a simple demonstration of the Analog Transducer. A transducer converts physical quantities to electrical signals and vice versa.

