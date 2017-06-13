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
 
The following sketch demonstrate a simple application using the Line Finder Sensor: whenever the Line Finder sees a black line, an LED will turn on and when the Line Finder sees a white line, the LED will turn off.
 
Demo code:
```java
import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup {
           
	public static Port LED_PORT = D4;
        public static Port LineFinderPort = D3;
        private final int LED_HIGH = LED.range()-1;       
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LineFinderPort);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value*LED_HIGH);
                System.out.println("In/Out of Line");
        });
    }
}
```			
When executed, the above code will cause the LED on D4 (digital output 4) to turn on when the Line Finder sensor on D3 (digital input 3) detects a black line.
 
The addDigitalListener() method returns a 1 as ```value``` when the Line Finder sensor detects a black line, and 0 for white lines. In order to turn on the LED on the digital port, we need to use setValue() method to send a value of 127 to the digital port connected to the LED.

 
 
 
 
 
 
