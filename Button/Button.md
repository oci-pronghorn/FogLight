# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example Project:
The following sketch demonstrates a simple application using the Button: whenever the Button is pressed, a relay will flash a light.
    Demon code:
```
package com.ociweb.grove;
import static com.ociweb.iot.grove.GroveTwig.Button;
import com.ociweb.iot.maker.*;
import com.ociweb.gl.api.GreenCommandChannel;
import static com.ociweb.iot.grove.GroveTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup
{
    private static final Port BUTTON_PORT = D3;
    private static final Port RELAY_PORT  = D7;
    
    @Override
    public void declareConnections(Hardware c) {
           
        c.connect(Button, BUTTON_PORT); 
        c.connect(Relay, RELAY_PORT);         
        c.useI2C();
        
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    
         final CommandChannel channel1 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
        runtime.addDigitalListener((port, connection, time, value)->{ 
            channel1.setValueAndBlock(RELAU_PORT, value == 1, 500);
        });
    }
}
```
When executed, the above code will cause the relay on D7 (digital output 7) to turn on when the button on D3 (digital input 3) is pressed.

The addDigitalListener() method returns a 1 as value when the button is pressed, and 0 when it is released. In order to send a signal to the relay on the digital port, we need to use setValue() method to send a value of 1 to the digital port connected to the relay.
