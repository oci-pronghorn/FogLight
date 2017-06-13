
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrate a simple application using the Line Finder Sensor: whenever the Line Finder sees a black line, an LED will turn on and when the Line Finder sees a white line, the LED will turn off.
 
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
When executed, the above code will cause the LED on D4 (digital output 4) to turn on when the Line Finder sensor on D3 (digital input 3) detects a black line.
 
The addDigitalListener() method returns a 1 as ```value``` when the Line Finder sensor detects a black line, and 0 for white lines. In order to turn on the LED on the digital port, we need to use setValue() method to send boolean value to the digital port connected to the LED (a _true_ will turn the LED on, while a _false_ will turn if off).

 
 
 
 
 
 
