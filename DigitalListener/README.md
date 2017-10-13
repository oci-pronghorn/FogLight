# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using a ```DigitalListener()```.

Demo code:


```java
package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Buzzer;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.TouchSensor;
import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D4;
import static com.ociweb.iot.maker.Port.DIGITALS;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class DigitalListener implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
	private static final Port BUZZER_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private static final Port TOUCH_SENSOR_PORT = D4;
	

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
    	c.connect(Buzzer, BUZZER_PORT);
    	c.connect(Button, BUTTON_PORT);
        c.connect(TouchSensor, TOUCH_SENSOR_PORT);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
    	runtime.addDigitalListener(new DigitalListenerBehavior(runtime)).includePorts(DIGITALS);

    }
          
}
```


Behavior class:


```java
package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class DigitalListenerBehavior implements DigitalListener {

	private static final Port BUZZER_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private static final Port TOUCH_SENSOR_PORT = D4;

	private FogCommandChannel channel;
	public DigitalListenerBehavior(FogRuntime runtime) {
		channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER | DYNAMIC_MESSAGING);
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		if(value == 1){
			channel.setValue(BUZZER_PORT, true);
			System.out.println("Digital event came from " + port);
			
			
		}
		else{
			channel.setValue(BUZZER_PORT, false);
			System.out.println("Buzzer was on for " + durationMillis + " milliseconds");
			System.out.println("time: " + time);
		}
	}

}
```


This class is a simple demonstration of how to use the ```DigitalListener()```. If either the touch sensor or the button is pressed, the LED will turn on and it will print which port device was used. After the touch sensor or button is released, the length of time the LED was on and the current epoch time will be printed in milliseconds.
In the behavior class, the overridden method will provide you with four variables, ```port```, ```time```, ```durationMillis```,  and ```value```. 
- ```port``` will give you the port from which the change in value came from.
- ```time``` will give the epoch time in milliseconds. 
- ```durationMillis``` will give the length of time the light was one
- ```value``` will give you the current value of the digital device that the listener is picking up, either a 1 or a 0. 
