# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The project turns the Raspberry Pi into a server and serves up readings from the Water Sensor Grove Twig on  <Pi's IP Address>/Water_Sensor.

.include "./src/main/java/com/ociweb/grove/IoTApp.java"


Since the IoT app needs to both exist AnalogListener behavior as well as well RestListener behavior, a custom listener (```RestfulWaterSensorBehavior```) that extends both is needed :

.include "./src/main/java/com/ociweb/grove/RestfulWaterSensorBehavior.java"

The constants needed for both classes can be consolidated in a third .java file:

.include "./src/main/java/com/ociweb/grove/RestfulWaterSensorConstants.java"
