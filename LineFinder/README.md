
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project (Door Monitor):

The following sketch demonstrate a simple application using the Line Finder Sensor: a black object covering the sensor will simulate door being closed, while an uncovered sensor will simulate the door being opened. The program can track the duration that the door has been opened/ closed.

Demo code:
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;



public class IoTApp implements FogApp {
    
    public static final Port LED_PORT = D4;
    public static final Port LINEFINDER_PORT = D3;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LINEFINDER_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        runtime.registerListener(new LineFinderBehavior(runtime));
        
    }
    
}```

Then specify the behavior of the program in the Behavior class:

```java
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import static com.ociweb.grove.IoTApp.*;


/**
 *
 * @author huydo
 */
public class LineFinderBehavior implements DigitalListener {
        
    private final FogCommandChannel ledChannel;
    
    public LineFinderBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel(PIN_WRITER);
    }
    @Override
    public void digitalEvent(Port port, long time, long durationMillis, int value) {
        ledChannel.setValue(LED_PORT,value==1);
        if(value == 1){
            System.out.println("Door just close. Time the door remained opened: "+durationMillis);
        }else{
            System.out.println("Door just open. Time the door remained closed: "+durationMillis);
        }
    }
    
}
```



Note: Make sure that the black object covers the sensor completely to trigger the sensor.

In digitalEvent() method of the Behavior class, ```value``` is set to 1 when the Line Finder sensor detects a black line, and 0 for white lines. Whenever ```value``` changes, digitalEvent() will be triggered and executes. The ```durationMillis``` indicates how long (in ms) that ```value``` remains unchanged before it changes.







