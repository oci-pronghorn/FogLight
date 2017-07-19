# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a simple use of the ```FogCommandChannel```.

Demo code:
Main Class

```
package com.coiweb.oe.foglight.api;
import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class CommandChannel implements FogApp
{

	private static final Port BUTTON_PORT = D3;
	private static final Port LED_PORT = D2;

    @Override
    public void declareConnections(Hardware c) {

    	c.connect(Button, BUTTON_PORT);
    	c.connect(LED, LED_PORT);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	
    	runtime.addDigitalListener(new CmdChannelBehavior(runtime));
    }
          
}
```

Behavior class

```
package com.coiweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class CmdChannelBehavior implements DigitalListener {
	
private static final Port LED_PORT = D2;
private static final Port BUTTON_PORT = D3;

	
	private final FogCommandChannel channel1;
	
	public CmdChannelBehavior(FogRuntime runtime) {
		channel1 = runtime.newCommandChannel( FogCommandChannel.PIN_WRITER | DYNAMIC_MESSAGING);
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {

		//channel1.setValueAndBlock(LED_PORT, value == 1, 500); //This method could be used on its own
		
		channel1.setValue(LED_PORT, value == 1); 
		
		channel1.block(500); //This block method will stop anything from going through this channel for the specified amount of milliseconds
		
		//channel1.blockUntil(1514764800000); //this will block until the specified epoch time, 
		
		//channel1.block(BUTTON_PORT, 500); //This block method only stop any commands for the specified port, but other uses of the command channel will still be active
		

	}

}

```

These classes are a basic demo of how to use the ```FogCommandChannel```. In the main class, a ```DigitalListener``` is called. Inside the the behavior class of that listener is an example of the command channel. Typically, only command channel will be needed per class. The command channel will be initialized in the constructor. After initializing it, you can use it throughout the entire class. Every command you send through it will be added to its que. However, the command channel can be "blocked", which means that for a specified amount of time, unti a certain time, or until a flag, that channel will not go to the next item in its que. You can see this in work with the above code. After the DigitalListener hears the change in the button, it will turn on the LED while it is pressed. However, if you were to repeatedly press the button quicker than 500 milliseconds, than the LED will not turn back on until after the block is over, making it appear like it is lagging behind the fast pace of clicks.
