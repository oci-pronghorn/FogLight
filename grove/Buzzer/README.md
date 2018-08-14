# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will turn the buzzer on whenever the button is pressed down.

Demo code:

```java
package com.ociweb.grove;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Buzzer;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D8;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port BUZZER_PORT = D8;

    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, BUTTON_PORT); 
        c.connect(Buzzer, BUZZER_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
  
        runtime.addDigitalListener(new BuzzerBehavior(runtime));
       
    }
}
```

Behavior class:

```java
package com.ociweb.grove;

import static com.ociweb.iot.maker.FogCommandChannel.PIN_WRITER;
import static com.ociweb.iot.maker.Port.D8;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class BuzzerBehavior implements DigitalListener {
	
	private static final Port BUZZER_PORT = D8;
	
	private final FogCommandChannel channel1;
	
	public BuzzerBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
        channel1 = runtime.newCommandChannel(PIN_WRITER);
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
	    channel1.setValueAndBlock(BUZZER_PORT, value == 1, 500);

	}

}
```


When executed, the above code will allow you to turn on the buzzer whenever the button is pressed. Also, whenever the button is released, there will be a 500 millisecond delay before the buzzer will turn on again.
The ```digitalEvent()``` method passes a 1 as the value when the button is pressed, and 0 when it is released. In order to send a signal to the relay on the digital port, use the ```setValue()``` method to check if the value is equivalent to 1, and when it is, a signal will be sent.
