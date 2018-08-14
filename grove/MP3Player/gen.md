# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to play MP3 files from a folder.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in MP3Player.java:

.include "./src/main/java/com/ociweb/grove/MP3Player.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/MP3Behavior.java"
.include "./src/main/java/com/ociweb/grove/MonitoringBehavior.java"

When executed, the MP3 player will play MP3 files from a folder. 
 
 
 
 
