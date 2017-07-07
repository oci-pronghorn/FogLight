# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example Project:
The following sketch demonstrates a simple application using the Relay: whenever the Button is pressed, the relay will flash a light.

Demo code:
```

```java
package com.ociweb.grove;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port RELAY_PORT  = D7;
    
    @Override
    public void declareConnections(Hardware c) {     
        c.connect(Button, BUTTON_PORT); 
        c.connect(Relay, RELAY_PORT);         
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        runtime.addDigitalListener((port, connection, time, value)->{ 
        	channel1.setValueAndBlock(RELAY_PORT, value == 1, 500);
        });
    }
}
```

```
When executed, the above code will cause the relay on D7 (digital output 7) to turn on when the button on D3 (digital input 3) is pressed.

