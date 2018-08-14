# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to control the velocity of two motors using an Angle Sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/MiniMotorBehavior.java"

The motors are controlled by the setVelocity() method. Turning the knob on the angle sensor will drive the motors forward/ backward with the adjustable speed. 

By implementing the MiniMotorDriverListener interface, the fault status of each channel can be checked. ch1Status/ch2Status passes a 1 when there's fault on the corresponding channel.
 
 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/mini_motor_driver/MiniMotorDriver_Transducer.java).
 
 
 
