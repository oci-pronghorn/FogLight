# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will demonstrate a simple use of the ```addSerialListener()``` method.
 
Demo code:
Main Class
```
package com.coiweb.oe.foglight.api;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class SerialListener implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {
        c.useSerial(Baud.B_____9600); //optional device can be set as the second argument       
        c.setTimerPulseRate(50);
        c.limitThreads();//pics optimal threads based on core detection
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	
    	runtime.addSerialListener(new SerialListenerBehavior(runtime));

        runtime.addTimePulseListener(new SerialWriterBehavior(runtime));
        
    }
          
}
```

Behavior classes 
```
package com.coiweb.oe.foglight.api;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.iot.maker.SerialReader;

public class SerialListenerBehavior implements SerialListener {

	private final FogRuntime runtime;
	private final static Logger logger = LoggerFactory.getLogger(SerialListenerBehavior.class);
	
	private byte[] myBuffer = new byte[10];
	private int timeToLive = 10;
	
	SerialListenerBehavior(FogRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public int message(SerialReader reader) {
		
		if (reader.available()<10) {
			return 0; //consumed nothing
		} else {
						
			int consumed = reader.read(myBuffer);
		
			logger.info("consumed data {} ", Arrays.toString(myBuffer));
			
			if (--timeToLive <= 0) {
				runtime.shutdownRuntime();
			}
			
			return consumed;			
		}
		
	}

}
```
```
package com.coiweb.oe.foglight.api;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.SERIAL_WRITER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialWriterBehavior implements TimeListener {

	private static Logger logger = LoggerFactory.getLogger(SerialWriterBehavior.class);
	private final FogCommandChannel cmd;
	private int value = 0;
	
	SerialWriterBehavior(FogRuntime runtime) {
		cmd = runtime.newCommandChannel(SERIAL_WRITER);
	}

	@Override
	public void timeEvent(long time, int iteration) {			
		if (!cmd.publishSerial(writer -> writer.writeByte(value++))) {
			logger.warn("unable to write to serial, the system is too busy");
		}
	}

}
```
