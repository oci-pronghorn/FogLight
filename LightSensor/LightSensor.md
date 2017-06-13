# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
# Example Project:
The following sketch demonstrates a simple application using the Light Sensor: as the Light Sensor runs, it will print out the values of the light in the room.
    Demo code:
```
package com.ociweb.grove;
import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup
{
    private static final Port LIGHT_SENSOR_PORT= A2;
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    @Override
    public void declareConnections(Hardware c) {         
        c.connect(LightSensor, LIGHT_SENSOR_PORT); 
        c.useI2C();
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	final CommandChannel lcdScreenChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addAnalogListener((port, time, durationMillis, average, value)->{
    		lcdScreenChannel.setValue(LIGHT_SENSOR_PORT, value);
    		System.out.println(value);
    	});
    }
}
```
When executed, the above code will cause the light sensor on A2 (analog output 8) to continuously print out the value of light present in the room.
 buzzer and relay.