# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 
-[Maven](https://maven.apache.org/install.html), which will help you download and manage the libraries and APIs you need to get your Grove device working.
-[Git](https://git-scm.com/), which will help you clone a template Maven project with the necessary dependencies already set up.
# Starting your Maven project:
 In the command line or terminal of your local machine, please enter:
```
 git clone https://github.com/oci-pronghorn/FogLighter.git
 cd FogLighter
 mvn install
 ```
 
Now, please ```cd``` into a directory that you would like your own IoT project to be created in, and enter:
```
mvn archetype:generate -DarchetypeGroupId=com.ociweb -DarchetypeArtifactId=FogLight-Archetype -DarchetypeVersion=0.1.0-SNAPSHOT
```
The terminal now asks you for: 
```groupID```: type in  *com.ociweb* then press Enter
```ArtifactID```: type in name of your project then press Enter
```version: 1.0-SNAPSHOT ```: Ignore, Press Enter
```package: com.ociweb ```: Ignore, Press Enter
```Y:```  :  Type *Y*, press Enter

This will create a folder named after your project, which includes all the project files. Let’s call our project *ProjectXYZ*.  
If you’re working from Terminal, open up the file  “ProjectXYZ”/src/main/java/com/ociweb/IoTApp.java . You can start implementing the project code from here. 
If you’re using an IDE, open up the created Maven project - *ProjectXYZ* and start working from IoTApp.java

Once you’re done with the implementation, open your project folder in terminal and type 
```
mvn install
```
.. to build the project. This will create a .jar file named ProjectXYZ.jar in the **/target** folder (note that there are other .jar files  in **/target**, but we don’t have to worry about those). Transfer this .jar file to your device and use the command 
```
java -jar ProjectXYZ.jar 
```
.. to execute it.
 
# Example project:
 
The following sketch demonstrates a simple application to control the Grove LED's brightness using PWM technique: the LED will fade in and out following a "sinusoidal" manner.
 
Demo code:
```java
import static com.ociweb.iot.grove.GroveTwig.*;
import java.lang.*;

import com.ociweb.gl.api.TimeTrigger;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.GreenCommandChannel;
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
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
           
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
First of all, note that PWM only works on the ports D3, D5 and D6. More details on the ports can be found [here](https://www.dexterindustries.com/GrovePi/engineering/port-description/). When using PWM, the LED is completely off at ```lightIntensity``` = 0, and reaches its peak brightness at ```lightIntensity``` = 127. 

The setTriggerRate(50) method forces addTimeListener() to execute every 50 ms. 

When executed, the above code will cause the LED's brightness to oscillate according to the following manner: starting from 0, the ```lightIntensity``` will increment by 1 every 50 ms. When ```lightIntensity``` reaches 127, it will start decrementing by 1 evert 50 ms until it reaches 0 and goes back up again. 
 

 
 
 
 
 
 
