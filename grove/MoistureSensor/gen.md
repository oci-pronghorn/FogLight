
# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The Moisture Sensor will return a ```value``` of 0 when it's dry. Then ```value``` will increase the moister the sensor is up to 1023.

Demo code (copy and paste this to a FogLighter project):

First declare the connections in IoTApp.java:

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:

.include "./src/main/java/com/ociweb/grove/MoistureSensorBehavior.java"   



 
 
 
 
