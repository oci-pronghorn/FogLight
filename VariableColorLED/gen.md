# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to control the Grove LED's brightness using PWM technique: the LED will fade in and out following a "sinusoidal" manner.
 
Demo code (copy and paste this to a FogLighter project):
First declare the connections in IoTApp.java:
.include "./src/main/java/com/ociweb/grove/IoTApp.java"

Then specify the behavior of the program in the Behavior class:
.include "./src/main/java/com/ociweb/grove/VariableColorLEDBehavior.java"

		
First of all, note that PWM only works on the ports D3, D5 and D6. More details on the ports can be found [here](https://www.dexterindustries.com/GrovePi/engineering/port-description/). When using PWM, the LED is completely off at ```lightIntensity``` = 0, and reaches its peak brightness at ```lightIntensity``` = 255 (which equals to LED.range()-1). 

The setTimerPulseRate(50) method forces the timeEvent() in Behavior class to execute every 50 ms. 

When executed, the above code will cause the LED's brightness to oscillate sinusoidally by using a sinusoidal function that takes ```time``` as an argument.
 
 
 
 
 
 
