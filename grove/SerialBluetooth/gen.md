# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application for a bluetooth device.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in SerialBluetooth.java:

.include "./src/main/java/com/ociweb/grove/SerialBluetooth.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/BluetoothBehavior.java"

When executed, a sensor will read bluetooth input and output.
 
 
 
 
