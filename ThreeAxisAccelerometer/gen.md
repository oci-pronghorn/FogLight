# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to detect if the accelerometer sensor is in free fall

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 

.include "./src/main/java/com/ocweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ocweb/grove/AccelerometerBehavior.java"

When executed, the program will print out the x,y,z acceleration values and notifies when the device is in free fall.

[1] The ThreeAxisAccelerometer source code supports 3 Listener interfaces:
1. AccelValsListener
This listener has accelVals() abstract method which returns x,y,z acceleration values in units of mg (1000 mg = 9.8 m/s^2)
2. AccelInterruptListener
The AccelInterruptStatus() abstract method has 5 fields corresponding to 5 types of interrupt. The int of the field will be 1 if the corresponding action is detected. 
3. ActTapListener
Each parameter in the abstract methods will be 1 if the corresponding activity/ inactivity on that axis is detected. 

 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/three_axis_accelerometer_16g/ThreeAxisAccelerometer_16g_Transducer.java).
 
 
 
