# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

-[Mosquitto](https://mosquitto.org/download/), which is an MQTT message broker

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
 The following sketch will demonstrate a basic demo for using the GPS.

Demo code:

.include "./src/main/java/com/ociweb/IoTApp.java"
Behavior class:

.include "./src/main/java/com/ociweb/FourDigitDisplayBehavior.java"

This is an example use of the four digit display. Like a digital device, you can use the method ```setValue()``` to change the values of the four digit display.