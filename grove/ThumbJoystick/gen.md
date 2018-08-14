# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch reads and prints the X and Y values of the Joystick. In addition, it detects presses.
 
Demo code: 

.include "./src/main/java/com/ociweb/grove/IoTApp.java"
 

The Joystick is made out of two potentiometers rotating in two orthogonal (X and Y) planes. They are physically constrained so that their values would read between around 200 to 800. When the joystick is pressed, the X value will read 1023 and can be used to detect presses.

The lambda passed into ```runtime.addAnalogListener()``` first identifies which Port (the X or Y port) triggered the analog event, and prints out the value accordingly. There is an addition conditional logic for X to determine whether the joystick was pressed.

 
 
 
 
 
 