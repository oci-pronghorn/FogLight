# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will turn the LED light on whenever the button is pressed down.

Demo code: 
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port LED_PORT    = D2;
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, BUTTON_PORT);
        c.connect(LED, LED_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        //this digital listener will get all the button press and un-press events
        runtime.addDigitalListener(new LEDBehavior(runtime));
        
    }
}
```


Then specify the behavior of the program in the Behavior class:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;
import static com.ociweb.iot.maker.FogCommandChannel.*;
public class LEDBehavior implements DigitalListener {
    
    private static final Port LED_PORT = D2;
    
    final FogCommandChannel channel1;
    
    public LEDBehavior(FogRuntime runtime) {
        // TODO Auto-generated constructor stub
        channel1 = runtime.newCommandChannel(PIN_WRITER);
        
    }
    
    @Override
    public void digitalEvent(Port port, long time, long durationMillis, int value) {
        // TODO Auto-generated method stub
        channel1.setValueAndBlock(LED_PORT, value == 1, 200);
        
    }
    
}
```


When executed, the above code will turn the LED light on while the button is pressed. After the light is turned off, there will also be a 200 millisecond delay before the LED light can be turned on again.

The addDigitalListener() method passes a 1 as value when the button is pressed, and 0 when it is released. In order to send a signal to the relay on the digital port, use the setValue() method to check if the value is equivalent to 1, and when it is, a signal will be sent.
