# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 The following sketch demonstrates a simple application to notify when an analog input value exceeds a specified threshold.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in AnalogToIIC.java . In order to perform I2C read, specify what to read in the connect() method. Then specify the corresponding listener^1^ for the Behavior class to implement. The i2c data read will then be passed to the listener interface's abstract methods. 

.include "./src/main/java/com/ociweb/grove/AnalogToIIC.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/AnalogToIICBehavior.java"

[1] The ADC source code supports 2 Listener interfaces:
1. ConversionResultListener
This listener has conversionResult() abstract method which passes the 12 bit integer conversion result from the ADC.

2. AlertStatusListener
This listener has alertStatus() abstract method which has 2 parameters: overRange and underRange. The value of the parameter will be 1 when the over range/under range event occurs. 
 
 For more information about the device's methods, refer to its javadocs [here](https://github.com/oci-pronghorn/FogLight/blob/master/src/main/java/com/ociweb/iot/grove/adc/ADC_Transducer.java).
 
 
 
