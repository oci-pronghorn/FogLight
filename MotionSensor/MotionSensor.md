
# Starting a FogLighter project using Maven:
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application using the Motion Sensor: whenever the Motion Sensor detects a movement, an LED light will turn on:

Demo code:

```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.MotionSensor;
import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {
           
	private static final Port LED_PORT = D4;
        private static final Port PIR_SENSOR = D3;
        
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(MotionSensor, PIR_SENSOR);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value==1);
                System.out.println("Stop moving!");                    	        	
        });
              
    }
    
}
```


When executed, the above code will cause the LED on D4 (digital output 4) to turn on when the motion sensor on D3 (digital input 3) detects a movement.

The addDigitalListener() method returns a 1 as ```value``` when the motion sensor detects a movement, and 0 otherwise. In order to turn on the LED on the digital port, we need to use setValue() method to send boolean value to the digital port connected to the LED (a _true_ will turn the LED on, while a _false_ will turn if off).







