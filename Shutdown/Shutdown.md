# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using a ```I2CListener()```.

Demo code:

```
package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;


public class Shutdown implements FogApp
{	
	private static final Port LED_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(Button, BUTTON_PORT);
    	c.connect(LED, LED_PORT);

    }
  
    @Override
    public void declareBehavior(final FogRuntime runtime) {
    	runtime.registerListener(new ShutdownBehavior(runtime)).addSubscription("LED");
    	
    }          

          
}

```
Behavior class:

```
package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class ShutdownBehavior implements StartupListener, DigitalListener, ShutdownListener, PubSubListener{

	private static final Port LED_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private boolean SdRequested = false; // shutdown requested
	private boolean PSRequested = false; // pubsub requested
	private boolean statusOfLED = false; // LED on or off
	private boolean SdConfirmed = false; //shutdown confirmed
	
	final FogCommandChannel channel1;

	private final FogRuntime runtime;

   
	
    public ShutdownBehavior(FogRuntime runtime) {
		channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING | FogCommandChannel.I2C_WRITER | FogCommandChannel.PIN_WRITER);
		this.runtime = runtime;
	}
	
	@Override
	public void startup() {
		channel1.setValue(LED_PORT, true);
		statusOfLED = true;
		System.out.println("The Light is on");
	}
	
	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		if(value == 1 && !SdRequested){
			System.out.println("Starting the shutdown process");
			SdRequested = true;
		
			runtime.shutdownRuntime();
    		
		}
	}
	
	@Override
	public boolean acceptShutdown() {
		if(statusOfLED){
			if(!PSRequested){
				PSRequested = true;
				System.out.println("Checking and turning off light");
    			
    			
				channel1.setValue(LED_PORT, false);   			
				
    			channel1.publishTopic("LED");
			}
			return false;
		}
		else if(!SdConfirmed){
			SdConfirmed = true;
			System.out.println("Shutting down");
			
			return true;
		}
		return false;
	}
	
	

	@Override
	public boolean message(CharSequence topic, BlobReader payload) {
		statusOfLED = false;
		return true;
	}
	
	

	
}

```
This class is a simple demonstration of how to use the ```I2CListener()```. This demonstration uses the I2C ADC, which is an analog to I2C converter. Also, take note that inside of the I2CListenerBehavior, there is also a startup listener which is necessary to begin an I2CListener. 
