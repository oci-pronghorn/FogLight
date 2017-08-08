# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate two simple uses of the addTimeListener() method.

Demo code: 


```java
package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Timer implements FogApp
{
	private static final long timeInterval = 60_000; //Time in milliseconds
	private static long startTime;
	private static boolean haveStartTime = false;

    @Override
    public void declareConnections(Hardware c) {
    	c.setTriggerRate(1); //the rate at which time is checked in milliseconds
        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {

    	runtime.addTimeListener((time, instance)->{
    		//Demo 1
    		if(time%timeInterval == 0){	
        		System.out.println("clock");
    		}
    		
    		////////////////////////////////////////
    		////////////////////////////////////////
    		
    		//Demo 2
    		//if((time-startTime)%timeInterval == 0){
    		//	System.out.println("clock");
    		//}
    		//if(!haveStartTime){
    		//	startTime = time;
    		//	haveStartTime = true;
    		//}
    	});
    }
}
```


The first demo in this code uses the addTimeListener() method to print out the string "clock" at the top of every minute, regardless of when the program was started. The second demo uses the addTimeListener() method to print out the string "clock" at an interval of one minute since the start of the program. You can change the interval length by changing timeInterval .
