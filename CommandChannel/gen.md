# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will demonstrate a simple use of the ```FogCommandChannel```.
 
Demo code:
Main Class

.includeFile "./src/main/java/com/ociweb/oe/foglight/api/CommandChannel.java"

Behavior class
.includeFile "./src/main/java/com/ociweb/oe/foglight/api/CmdChannelBehavior.java"

These classes are a basic demo of how to use the ```FogCommandChannel```. In the main class, a ```DigitalListener``` is called. Inside the the behavior class of that listener is an example of the command channel. Typically, only command channel will be needed per class. The command channel will be initialized in the constructor. After initializing it, you can use it throughout the entire class. Every command you send through it will be added to its que. However, the command channel can be "blocked", which means that for a specified amount of time, unti a certain time, or until a flag, that channel will not go to the next item in its que. You can see this in work with the above code. After the DigitalListener hears the change in the button, it will turn on the LED while it is pressed. However, if you were to repeatedly press the button quicker than 500 milliseconds, than the LED will not turn back on until after the block is over, making it appear like it is lagging behind the fast pace of clicks.
