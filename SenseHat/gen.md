# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to set up a SenseHat for basic use.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in SenseHat.java:

.include "./src/main/java/com/ociweb/grove/SenseHat.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/SenseHatBehavior.java"

When executed, the SenseHat can be used to display sensor data via LED display. Measures environmental conditions (temperature and humidity).
 
 
 
 
