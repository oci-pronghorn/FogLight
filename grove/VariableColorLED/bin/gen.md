# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
 
# Example project:
 
The following sketch demonstrates a simple application to control the Grove LED's brightness using PWM technique: the LED will fade in and out following a "sinusoidal" manner.
 
Demo code (copy and paste this to a FogLighter project):
.include "./src/main/java/com/ociweb/grove/IoTApp.java"
		
First of all, note that PWM only works on the ports D3, D5 and D6. More details on the ports can be found [here](https://www.dexterindustries.com/GrovePi/engineering/port-description/). When using PWM, the LED is completely off at ```lightIntensity``` = 0, and reaches its peak brightness at ```lightIntensity``` = 255 (which equals to LED.range()-1). 

The setTriggerRate(50) method forces the lambda which was passed to addTimeListener() to execute every 50 ms. 

When executed, the above code will cause the LED's brightness to oscillate according to the following manner: starting from 0, the ```lightIntensity``` will increment by 1 every 50 ms. When ```lightIntensity``` reaches 255, it will start decrementing by 1 evert 50 ms until it reaches 0 and goes back up again. 
 
Note: Another method of making the LED's brightness oscillate is to use a sinuisoidal function that takes ```time``` as an argument.
 
 
 
 
 
 
