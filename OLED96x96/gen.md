# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

-[Mosquitto](https://mosquitto.org/download/), which is an MQTT message broker

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
 The following sketch will demonstrate a basic demo for using an OLED 96 x 96.

Demo code:

.include "./src/main/java/com/ociweb/grove/OLED96x96.java"

Behavior class:

.include "./src/main/java/com/ociweb/grove/OLED_96x96Behavior.java"

This is an example use of the OLED 96 x 96. In the behavior class, make sure to an OLED 96x96 transducer. Also included in the class, but while not shown above, are multiple 2-demensional arrays of different images. You can generate your own 2-demensional array of a jpeg or a png file to put the image on the OLED 96x96 by going to AppTest under ./src/test/java/com/ociweb/grove/.