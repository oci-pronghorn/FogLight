
# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project (Door Monitor):
 
The following sketch demonstrate a simple application using the Line Finder Sensor: a black object covering the sensor will simulate door being closed, while an uncovered sensor will simulate the door being opened. The program can track the duration that the door has been opened/ closed.
 
Demo code:
.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Note: Make sure that the black object covers the sensor completely to trigger the sensor.

The addDigitalListener() method returns a 1 as ```value``` when the Line Finder sensor detects a black line, and 0 for white lines. Whenever ```value``` changes, the lambda which was passed to addDigitalListener() executes. The ```durationMillis``` indicates how long (in ms) that ```value``` remains unchanged before it changes.

 
 
 
 
 
 
