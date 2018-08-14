# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to measure the depth of water in a tank and output it on an LCD screen.

Demo code (copy and paste this to a FogLighter project). There will be 2 .java files in the source code folder: IoTApp.java and IoTBehavior.java:

In IoTApp.java:
.include "./src/main/java/com/ociweb/grove/IoTApp.java"

In IoTBehavior.java:

.include "./src/main/java/com/ociweb/grove/IoTBehavior.java"

The sensor returns ```value``` which is the distance between the sensor and the water surface in _cm_
 
 
 
 
 
 
