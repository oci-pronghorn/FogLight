# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to control the Grove LED's brightness using PWM technique: the LED will fade in and out following a "sinusoidal" manner.
 
Demo code (copy and paste this to a FogLighter project):
```java
import static com.ociweb.iot.grove.GroveTwig.*;
import java.lang.*;
import com.ociweb.gl.api.TimeTrigger;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IoTApp implements IoTSetup {
    public static Port LED_PORT = D3;
    private int lightIntensity = 0;
    private boolean brighter = true;
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.setTriggerRate(50);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
           
        runtime.addTimeListener((time)->{
            if (lightIntensity == 0 || brighter){                
                lightIntensity += 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == LED.range()-1){
                    brighter = false;
                }
            }else{
                lightIntensity -= 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == 0){
                    brighter = true;
                }
            }            
            });            
    }
}
```			
First of all, note that PWM only works on the ports D3, D5 and D6. More details on the ports can be found [here](https://www.dexterindustries.com/GrovePi/engineering/port-description/). When using PWM, the LED is completely off at ```lightIntensity``` = 0, and reaches its peak brightness at ```lightIntensity``` = 255 (which equals to LED.range()-1). 

The setTriggerRate(50) method forces the lambda which was passed to addTimeListener() to execute every 50 ms. 

When executed, the above code will cause the LED's brightness to oscillate according to the following manner: starting from 0, the ```lightIntensity``` will increment by 1 every 50 ms. When ```lightIntensity``` reaches 255, it will start decrementing by 1 evert 50 ms until it reaches 0 and goes back up again. 
 

 
 
 
 
 
 
