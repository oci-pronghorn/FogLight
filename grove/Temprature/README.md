# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to measure the temperature in Celsius.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:


```java
package com.ociweb.grove;


import static com.ociweb.iot.maker.Port.A0;

import com.ociweb.iot.grove.temp_and_humid.TempAndHumidTwig;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        //c.connect(new TempAndHumidTwig(A0.port,TempAndHumidTwig.MODULE_TYPE.DHT2301));
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new TempSensorBehavior(runtime));

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

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

/**
 *
 * @author huydo
 */
public class TempSensorBehavior implements AnalogListener {
    public TempSensorBehavior(FogRuntime runtime) {   
    }
    private final int B = 4275;               // B value of the thermistor
    private final int R0 = 100000;            // R0 = 100k
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        // TODO Auto-generated method stub
        double R =  (1023.0/value-1.0);
        R = R0*R;
        double temperature = 1.0/(Math.log(R/R0)/B+1/298.15)-273.15; // convert to temperature via datasheet
        System.out.println("The temperature is : "+temperature+" Celsius");
    }
}
```

