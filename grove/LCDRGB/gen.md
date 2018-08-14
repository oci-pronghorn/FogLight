# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to print text on a LCDRGB display.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in LCDRGB.java:

.include "./src/main/java/com/ociweb/grove/LCDRGB.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/LCDRGB_Behavior.java"

When executed, the display will print out "Hello Walls".
 
 
 
 
