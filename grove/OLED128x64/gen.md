# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

-[Mosquitto](https://mosquitto.org/download/), which is an MQTT message broker

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
 The following sketch will demonstrate a basic demo for using an OLED 128x64.

Demo code:

.include "./src/main/java/com/ociweb/grove/OLED128x64.java"

Behavior class:

.include "./src/main/java/com/ociweb/grove/GameOfLifeBehavior.java"

This is an example use of the OLED 128 x 64. in the behavior class, the rules of the Game of Life are put into different methods, changing which cells are "alive" and which cells are "dead". 
Also, if you feed the OLED a 2-dimensional array of 1's and 0's that is 128 x 64, it will display it.
