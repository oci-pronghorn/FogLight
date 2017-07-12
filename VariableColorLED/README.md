# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to control the Grove LED's brightness using PWM technique: the LED will fade in and out following a "sinusoidal" manner.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:

```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {

	public static final Port LED_PORT = D3;
	private int lightIntensity = 0;
	public static void main( String[] args) {
		FogRuntime.run(new IoTApp());
	}    

	@Override
	public void declareConnections(Hardware hardware) {
		hardware.connect(LED, LED_PORT);
		hardware.setTimerPulseRate(50);
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
            runtime.registerListener(new VariableColorLEDBehavior(runtime));
		         
	}
}

```


Then specify the behavior of the program in the Behavior class:

```java
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

import static com.ociweb.grove.IoTApp.*;

/**
 *
 * @author huydo
 */
public class VariableColorLEDBehavior implements TimeListener {
    
    private int lightIntensity = 0;
    
    private final FogCommandChannel ledChannel;
    
    public VariableColorLEDBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel();
    }
    
    @Override
    public void timeEvent(long time, int i) {
        lightIntensity = (int) (127* Math.sin(time/(Math.PI * 500)) + 127);
        System.out.println(lightIntensity);
        ledChannel.setValue(LED_PORT, lightIntensity);
    }
    
}
```



First of all, note that PWM only works on the ports D3, D5 and D6. More details on the ports can be found [here](https://www.dexterindustries.com/GrovePi/engineering/port-description/). When using PWM, the LED is completely off at ```lightIntensity``` = 0, and reaches its peak brightness at ```lightIntensity``` = 255 (which equals to LED.range()-1). 

The setTimerPulseRate(50) method forces the timeEvent() in Behavior class to execute every 50 ms. 

When executed, the above code will cause the LED's brightness to oscillate sinusoidally by using a sinusoidal function that takes ```time``` as an argument.






