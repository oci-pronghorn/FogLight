# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to read the time from the real time clock (RTC) device

Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 

.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/ClockBehavior.java"

The clock's time can be set using the setTime() method. 
The time readings are passed via the clockVals() method from the RTCListener interface. 

 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/real_time_clock/RTC_Transducer.java).
 
 
 
