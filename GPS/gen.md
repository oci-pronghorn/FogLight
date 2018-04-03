# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:

The following sketch demonstrates a simple application to identify GPS coordinates of a sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in GPS.java:

.include "./src/main/java/com/ociweb/grove/GPS.java"

Then specify the behavior of the program in the GPSBehavior class:
.include "./src/main/java/com/ociweb/grove/GPSBehavior.java"

When executed, the GPS sensor will return longitude and latitude values of the location of the sensor back to the executor.

 
 
 
