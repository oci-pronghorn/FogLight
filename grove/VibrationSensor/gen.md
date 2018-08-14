# What you will need before you start:
- [**Java 8**](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

- [**Maven**](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

- [**Git**](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.
# Hardware Needed
- [**Raspberry Pi**](https://www.raspberrypi.org/)
- [**GrovePi+ Board**](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting your Maven project: 
[Starting a FogLight project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
Demo code:
.include "./src/main/java/com/ociweb/grove/IoTApp.java"
Behavior class:
.include "./src/main/java/com/ociweb/grove/VibrationSensorBehavior.java"

The behavior class triggers the buzzer if the value of vibration is over a predetermined threshold, which is 800 in this case. 
