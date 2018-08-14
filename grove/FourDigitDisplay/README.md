# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

-[Mosquitto](https://mosquitto.org/download/), which is an MQTT message broker

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using the Four Digit Display.

Demo code:


```java
package com.ociweb;

import static com.ociweb.iot.maker.Port.D5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
public class IoTApp implements FogApp
{
	private static final Logger logger = LoggerFactory.getLogger(IoTApp.class);

	private final Port display_port = D5;
	@Override
	public void declareConnections(Hardware c) {
		
	}
	
	public static void main(String[] args){
		FogRuntime.run(new IoTApp());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new FourDigitDisplayBehavior(runtime, display_port));
	}
}
```

Behavior class:


```java
package com.ociweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.four_digit_display.FourDigitDisplayCommand.*;

import static com.ociweb.iot.maker.FogCommandChannel.*;

public class FourDigitDisplayBehavior implements TimeListener,StartupListener {

	private static final Logger logger = LoggerFactory.getLogger(FourDigitDisplayBehavior.class);

	private final FogCommandChannel ch;
	private final Port p;

	
	public FourDigitDisplayBehavior(FogRuntime r, Port p){
		this.ch = r.newCommandChannel(PIN_WRITER);
		this.p = p;
	}
	@Override
	public void timeEvent(long time, int iteration) {
		
		ch.setValue(p,iteration % 1000);

	}
	@Override
	public void startup() {
		ch.setValue(p, INIT);
		ch.setValue(p, SET_BRIGHTNESS + 7);
		ch.setValue(p, DISPLAY_ON);
	}
}
```


This is an example use of the four digit display. Like a digital device, you can use the method ```setValue()``` to change the values of the four digit display.
