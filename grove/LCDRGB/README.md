# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to print text on a LCDRGB display.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in LCDRGB.java:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class LCDRGB implements FogApp
{
   


    @Override
    public void declareConnections(Hardware c) {
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new LCDRGB_Behavior(runtime));
    }
          
}
```


Then specify the behavior of the program in the Behavior class:

```java
package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

import static com.ociweb.iot.grove.lcd_rgb.LCD_RGB_Twig.*;

public class LCDRGB_Behavior implements Behavior, StartupListener {

	private LCD_RGB_Transducer lcd;

	
	public LCDRGB_Behavior(FogRuntime rt){
		lcd = LCD_RGB.newTransducer(rt.newCommandChannel());	
	}


	@Override
	public void startup() {
		lcd.setCursor(0, 0);
		lcd.commandForTextAndColor("Hello Walls.", 63, 63, 127);
	}
	

	
		
	

}
```


When executed, the display will print out "Hello Walls".




