# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to control a stepper motor using the Motor Driver.


Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:

.include "./src/main/java/com/ociweb/grove/MotorDriverBehavior.java"

When executed, the stepper motor will turn 250 steps in the forward direction, then turn 250 steps backwards.

 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/motor_driver/MotorDriver_Transducer.java) .
 
 
 
