# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to notify when an analog input value exceeds a specified threshold.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in AnalogToIIC.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 


```java
package com.ociweb.grove;

import static com.ociweb.iot.grove.adc.ADCTwig.*;

import com.ociweb.iot.maker.*;

public class AnalogToIIC implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {

        c.connect(ADC.ReadConversionResult);
        c.connect(ADC.ReadAlertStatus);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AnalogToIICBehavior(runtime));

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

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.grove.adc.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class AnalogToIICBehavior implements StartupListener,AlertStatusListener,ConversionResultListener{
    private final FogCommandChannel ch;
    private final ADC_Transducer sensor;
    private final int upperLimit = 400;
    public AnalogToIICBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        sensor = new ADC_Transducer(ch,this);
    }
    @Override
    public void startup() {
        sensor.setAlertHoldBit(true);
        sensor.setAlertFlagEnableBit(true);
        sensor.setUpperLimit(upperLimit);
    }


    @Override
    public void conversionResult(int result) {

    }

    @Override
    public void alertStatus(int overRange, int underRange) {
        if(overRange == 1){
            System.out.println("clap detected");
            sensor.setAlertHoldBit(false);
            sensor.setAlertHoldBit(true);
        }
    }

}
```


[1] The ADC source code supports 2 Listener interfaces:
1. ConversionResultListener
This listener has conversionResult() abstract method which passes the 12 bit integer conversion result from the ADC.

2. AlertStatusListener
This listener has alertStatus() abstract method which has 2 parameters: overRange and underRange. The value of the parameter will be 1 when the over range/under range event occurs. 

For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/adc/ADC_Transducer.java).



