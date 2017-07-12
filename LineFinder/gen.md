
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project (Door Monitor):
 
The following sketch demonstrate a simple application using the Line Finder Sensor: a black object covering the sensor will simulate door being closed, while an uncovered sensor will simulate the door being opened. The program can track the duration that the door has been opened/ closed.
 
Demo code:
First declare the connections in IoTApp.java:

.include "./src/main/java/com/ociweb/grove/IoTApp.java"
Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/LineFinderBehavior.java"


Note: Make sure that the black object covers the sensor completely to trigger the sensor.

In digitalEvent() method of the Behavior class, ```value``` is set to 1 when the Line Finder sensor detects a black line, and 0 for white lines. Whenever ```value``` changes, digitalEvent() will be triggered and executes. The ```durationMillis``` indicates how long (in ms) that ```value``` remains unchanged before it changes.

 
 
 
 
 
 
