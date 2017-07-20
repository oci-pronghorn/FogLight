# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will demonstrate how to declare connections to different types of devices.
 
Demo code:

.include "./src/main/java/com/ociweb/oe/foglight/api/DeclareConnections.java

This class is a demonstration of how to declare connections to analog and digital devices, I2C devices, and serial devices. Anything that has been identified as optional is not needed to declare a connection, but can give added utility. 
