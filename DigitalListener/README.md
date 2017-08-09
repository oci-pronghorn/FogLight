# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using a ```DigitalListener()```.

Demo code:

#### ERROR:  could not read file ./src/main/java/com/ociweb/oe/foglight/api/DigitalListener.java

Behavior class:

#### ERROR:  could not read file ./src/main/java/com/ociweb/oe/foglight/api/DigitalListenerBehavior.java

This class is a simple demonstration of how to use the ```DigitalListener()```. If either the touch sensor or the button is pressed, the LED will turn on and it will print which port device was used. After the touch sensor or button is released, the length of time the LED was on and the current epoch time will be printed in milliseconds.
In the behavior class, the overridden method will provide you with four variables, ```port```, ```time```, ```durationMillis```,  and ```value```. 
- ```port``` will give you the port from which the change in value came from.
- ```time``` will give the epoch time in milliseconds. 
- ```durationMillis``` will give the length of time the light was one
- ```value``` will give you the current value of the digital device that the listener is picking up, either a 1 or a 0. 
