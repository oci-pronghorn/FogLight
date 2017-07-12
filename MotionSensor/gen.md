
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application using the Motion Sensor: whenever the Motion Sensor detects a movement, an LED light will turn on:
 
Demo code: 
First declare the connections in IoTApp.java:

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/MotionSensorBehavior.java"


When executed, the above code will cause the LED on D4 (digital output 4) to turn on when the motion sensor on D3 (digital input 3) detects a movement. 
 
In digitalEvent() method of the Behavior class,  ```value``` is set to 1 when the motion sensor detects a movement, and 0 otherwise. In order to turn on the LED on the digital port, we need to use setValue() method to send boolean value to the digital port connected to the LED (a _true_ will turn the LED on, while a _false_ will turn if off).

 
 
 
 
 
 
