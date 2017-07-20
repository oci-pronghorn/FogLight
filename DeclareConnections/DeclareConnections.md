# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate how to declare connections to different types of devices.

Demo code:

```
package com.coiweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.ThreeAxisAccelerometer_16g;

public class DeclareConnections implements FogApp
{
    
	private static final Port BUZZER_PORT = D2;	//Declaring these variables allows eaiser use in the main body of your code
	private static final Port BUTTON_PORT = D3;
	


    @Override
    public void declareConnections(Hardware c) {

    	//Declaring connections for analog or digital devices
    	c.connect(Buzzer, BUZZER_PORT);
    	c.connect(Button, BUTTON_PORT, 1, 1, true); //Optional: customRateMS - set the rate at which the device updates
    												//customAvgWinMS - set the rate at which the data is sampled
    												//everyValue - can cause the device to trigger events on every update
    	
    	//Declaring connections for I2C devices
    	c.useI2C();
    	c.connect(OLED_96x96, 1); //Optional: customRateMS - set the rate at which the device updates
    	
    	//Declaring connections for serial devices
    	c.useSerial(Baud.B__1000000, "GPS"); //Optional: the device can be set as a second argument
    	c.limitThreads(); //picks the optimal threads based on core detection, however, you can also add an int argument 
    					  //to assign a specific amount of threads
    	
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    }
          
}

```
This class is a demonstration of how to declare connections to analog and digital devices, I2C devices, and serial devices. Anything that has been identified as optional is not needed to declare a connection, but can give added utility. 
