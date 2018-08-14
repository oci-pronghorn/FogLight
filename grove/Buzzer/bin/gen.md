# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:
 
The following sketch will turn the buzzer on whenever the button is pressed down.
 
Demo code: 

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

When executed, the above code will allow you to turn on the buzzer whenever the butto is pressed. Also, whenever the button is released, there will be a 500 millisecond delay before the buzzer will turn on again.
The addDigitalListener() method passes a 1 as value when the button is pressed, and 0 when it is released. In order to send a signal to the relay on the digital port, use the setValue() method to check if the value is equivalent to 1, and when it is, a signal will be sent.
