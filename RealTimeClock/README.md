# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
The following sketch demonstrates a simple application to read the time from the real time clock (RTC) device

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 


```java
package com.ociweb.grove;


import static com.ociweb.iot.grove.real_time_clock.RTCTwig.*;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.
        c.connect(RTC.ReadTime);
        c.enableTelemetry();
        if(c instanceof TestHardware){
            byte[] dummy ={0};
            ((TestHardware) c).setI2CValueToRead((byte)104,dummy,1);
        }
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new ClockBehavior(runtime));
        
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


import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.real_time_clock.RTCListener;
import com.ociweb.iot.grove.real_time_clock.RTC_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class ClockBehavior implements RTCListener,Behavior{

    private FogCommandChannel ch;
    RTC_Transducer clock;
    public ClockBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        clock = new RTC_Transducer(ch,this);
    }

    @Override
    public void clockVals(int second, int minute, int hour, String dayOfWeek, int dayOfMonth, int month, int year) {
        StringBuilder indicator = new StringBuilder();
        
        indicator.append("The current time is:  ");
        indicator.append(dayOfWeek);
        indicator.append(", ");
        indicator.append(month);
        indicator.append("/");
        indicator.append(dayOfMonth);
        indicator.append("/");
        indicator.append(year);
        
        indicator.append("  ");
        indicator.append(hour);
        indicator.append(":");
        indicator.append(minute);
        indicator.append(":");
        indicator.append(second);
        
        System.out.println(indicator);
    }

}
```


The clock's time can be set using the setTime() method. 
The time readings are passed via the clockVals() method from the RTCListener interface. 

For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/real_time_clock/RTC_Transducer.java).



