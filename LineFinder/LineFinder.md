
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project (Door Monitor):
 
The following sketch demonstrate a simple application using the Line Finder Sensor: a black object covering the sensor will simulate door being closed, while an uncovered sensor will simulate the door being opened. The program can track the duration that the door has been opened/ closed.
 
Demo code:
```java
import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup {
    
    private static final Port LED_PORT = D4;
    private static final Port LINEFINDER_PORT = D3;
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {        
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LINEFINDER_PORT);        
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value==1);
                System.out.println("In/Out of Line");                
        });         
    } 
}
```			
Note: Make sure that the black object covers the sensor completely to trigger the sensor.

The addDigitalListener() method returns a 1 as ```value``` when the Line Finder sensor detects a black line, and 0 for white lines. Whenever ```value``` changes, the lambda which was passed to addDigitalListener() executes. The ```durationMillis``` indicates how long (in ms) that ```value``` remains unchanged before it changes.

 
 
 
 
 
 
