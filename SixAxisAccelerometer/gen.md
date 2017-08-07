# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to detect if the accelerometer sensor is in free fall

Demo code (copy and paste this to a FogLighter project):
First declare the connections in SixAxisAccelerometer.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 

.include "./src/main/java/com/ociweb/grove/SixAxisAccelerometer.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/AccelBehavior.java"

When executed, the program will print out the x,y,z acceleration values and the heading direction relative to the magnetic North in degrees.

[1] The SixAxisAccelerometer source code supports 2 Listener interfaces:
1. AccelValsListener
This listener has accelVals() abstract method which returns x,y,z acceleration values. In order to convert the values to units of g (1g = 9.8 m/s^2), divide the accel scale (2,4,6,8 or 16) by 65535 and multiply it by the x,y,z returned value from the listener. 

2. MagValsListener
This listener has magVals() abstract method which returns x,y,z magnetic values. In order to convert the values to units of Gauss, divide the magnetic scale (2,4,8 or 12) by 65535 and multiply it by the x,y,z returned value from the listener. 


 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/three_axis_accelerometer_16g/ThreeAxisAccelerometer_16g_Transducer.java).
 
 
 
