# Starting your Maven project: 
[Instructions here](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
# Example Project:
The following sketch demonstrates a simple application using the Light Sensor: when the light sensor detects a dark enough room, the LED light will turn on, otherwise the LED will stay off.
Demo code:

```java
package com.ociweb.grove;
import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.LightSensor;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.A2;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
    private static final Port LIGHT_SENSOR_PORT= A2;
    private static final Port LED_PORT = D2;
   
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    @Override
    public void declareConnections(Hardware c) {         
        c.connect(LightSensor, LIGHT_SENSOR_PORT);
        c.connect(LED, LED_PORT, 200); //200 is the rate in milliseconds to update the device data
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.addAnalogListener(new LightSensorBehavior(runtime));
    	
    }
}
```

Behavior class:

```java
package com.ociweb.grove;

import static com.ociweb.iot.maker.FogCommandChannel.PIN_WRITER;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class LightSensorBehavior implements AnalogListener {
	
	private static final Port LED_PORT = D2;
    private static final int darkValue = 350; //Light reading that will turn on the light

	final FogCommandChannel lcdScreenChannel;

	public LightSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
		lcdScreenChannel = runtime.newCommandChannel(PIN_WRITER);
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		// TODO Auto-generated method stub
		lcdScreenChannel.setValue(LED_PORT, value < darkValue);
		System.out.println(value);
	}

}
```


When executed, the above code will cause the light sensor on A2 (analog output 2) to continuously print out the value of light present in the room. If the value drops below the darkValue (350), the LED light on D2 (digatal output 2) will turn on. 
