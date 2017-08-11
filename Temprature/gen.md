# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to measure the temperature in Celsius.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/TempSensorBehavior.java"
