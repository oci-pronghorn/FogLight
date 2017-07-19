# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a simple use of the ```StateChangeListener```.

Demo code:
Main Class


```java
package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import com.ociweb.oe.foglight.api.StateMachine.StopLight;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.StateChangeListener;

public class StateMachine implements FogApp
{

	static String cGreen = "Green";
	static String cYellow = "Yellow";
	static String cRed = "Red";
	
	public enum StopLight{
		
		Go(cGreen), 
		Caution(cYellow), 
		Stop(cRed);
		
		private String color;
		
		StopLight(String lightColor){
			color = lightColor;
		}
		
		public String getColor(){
			return color;
		}
	}

	
    @Override
    public void declareConnections(Hardware c) {
    	
    	c.startStateMachineWith(StopLight.Stop);
    	c.setTimerPulseRate(1);
    }

   
	@SuppressWarnings("unchecked")
	@Override
    public void declareBehavior(FogRuntime runtime) {

        
        runtime.addTimePulseListener(new TimingBehavior(runtime));
		runtime.addStateChangeListener(new StateChangeBehavior());
    }
          
}
```


Behavior classes

#### ERROR:  could not read file ./src/main/java/com/coiweb/oe/foglight/api/TimingBehavior.java

#### ERROR:  could not read file ./src/main/java/com/coiweb/oe/foglight/api/StateChangeBehavior.java

These classes are a basic demo of how to use the ```StateChangeListener``` method. In the main class, a stop light is simulated with 3 different states, ```Go```, ```Caution```, and ```Stop```. In the ```declareConnections``` section, the stop light is initialized to the ```Stop``` state to beging with. If a state is initilized there, you use a ```changeState()``` in a StartupListener as the two will clash when starting the program, so you must use one or the other. In the ```TimeBehavior``` class, a TimeListener is being used to change the state the state of the stop light. Every 5 seconds, the state is changed to the next state in the pregression. In the ```StateChangeBehavior``` class, there is a StateChangeListener. Whenever it hears a change in state, it will print the new states color and will return true.
