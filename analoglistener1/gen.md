# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will demonstrate a simple use of the ```addAnalogListener()``` method.
 
Demo code:
Main Class

.include "./src/main/java/com/ociweb/oe/foglight/api/AnalogListener.java"

Behavior class 

.include "./src/main/java/com/ociweb/oe/foglight/api/AnalogListenerBehavior.java"

These classes are a basic demo of how to use the ```AnalogListener() method```. Following it with the ```includePorts()```method will cause that listener to only listen to the ports listed. Similarly, the ```ecludePorts()``` will make that listener ignore anything coming from any ports listed. Without either of those methods, the AnalogListener will listen to any analog devices connected to any analog compatible ports. 
In the behavior class, the overridden method will provide you with five variables, ```port```,  ```time```, ```durationMillis```, ```average```, and ```value```. 
- ```port``` will give you the port from which the change in value came from.
- ```time``` will give the length of time in milliseconds that the ```value``` has been/was at that specifc value. 
- ```durationMillis``` will give ... 
- ```average``` will give the current average of of all of the values. 
- ```value``` will give you the current value of the analog device that the listener is picking up. 
