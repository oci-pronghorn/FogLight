# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will demonstrate a simple use of the addStateChangeListener() method.
 
Demo code: 
```
.include "./src/main/java/com/ociweb/oe/foglight/StateMachine.java"
```
The above code simulates a stop light, changing between the different enums, ```Go```, ```Caution```, and ```Stop```. The ```StateChangeListener()``` will listen for any change in the state of an enum. In this demo, each change will also trigger another change in the state, however, by blocking the channel, the next change in state will not be immedeate. 