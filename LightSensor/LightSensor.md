# Starting your Maven project: 
[Instructions here](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
# Example Project:
The following sketch demonstrates a simple application using the Light Sensor: when the light sensor detects a dark enough room, the LED light will turn on, otherwise the LED will stay off.
    Demo code:
```
package com.ociweb.grove;
import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup
{
    private static final Port LIGHT_SENSOR_PORT= A2;
    private static final Port LED_PORT = D2;
   
    private static final int darkValue = 350; //Light reading that will turn on the light
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    @Override
    public void declareConnections(Hardware c) {         
        c.connect(LightSensor, LIGHT_SENSOR_PORT);
        c.connect(LED, LED_PORT, 200, 1000); 
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	final CommandChannel lcdScreenChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	runtime.addAnalogListener((port, time, durationMillis, average, value)->{
    		lcdScreenChannel.setValue(LED_PORT, value < darkValue);
    		System.out.println(value);
    	});
    }
}
```
When executed, the above code will cause the light sensor on A2 (analog output 2) to continuously print out the value of light present in the room. If the value drops below the darkValue (350), the LED light on D2 (digatal output 2) will turn on. 
