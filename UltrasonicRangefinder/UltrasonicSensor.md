# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to measure the depth of water in a tank and output it on an LCD screen.

Demo code (copy and paste this to a FogLighter project):
In IoTApp.java:
```java

import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.UltrasonicRanger;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;

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
        c.connect(UltrasonicRanger, A0);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new IoTBehavior(runtime));
    }
}

```
In IoTBehavior.java:

```java

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.util.Appendables;

public class IoTBehavior implements AnalogListener{
    
    private final FogCommandChannel channel;
    
    private final int fullTank = 25;
    
    public IoTBehavior(FogRuntime runtime) {
        
        channel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
       
    }
    
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        if (value>fullTank) {
            System.out.println("Check equipment, tank is deeper than expected");
        } else {
            int remainingDepth = fullTank-value;
            
            StringBuilder builder = new StringBuilder();
            Appendables.appendFixedDecimalDigits(builder, remainingDepth, 100);
            
            builder.append("cm \n");
            builder.append("depth");
            
            Grove_LCD_RGB.commandForColor(channel, 200, 200, 180);
            Grove_LCD_RGB.commandForText(channel, builder);   
        }   
    }
}

```			
The sensor returns ```value``` which is the distance between the sensor and the water surface in _cm_
 
 
 
 
 
 
