# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using an OLED 96 x 96.

Demo code:


```java
package com.ociweb.grove;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OLED96x96 implements FogApp
{
	private static final Logger logger = LoggerFactory.getLogger(OLED96x96.class);
	
	@Override
	public void declareConnections(Hardware c) {
		c.setTimerPulseRate(2500);
	}
	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new OLED_96x96Behavior(runtime));
	}
	public static void main(String[] args){
		FogRuntime.run(new OLED96x96());
	}
}
```


Behavior class:


```java
package com.ociweb.grove;

import com.ociweb.gl.api.TimeListener;
import static com.ociweb.iot.grove.oled.OLEDTwig.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.grove.oled.OLED_96x96_Transducer;
import static com.ociweb.grove.OCI_Logo.*;
import static com.ociweb.grove.Grumpy.*;
import static com.ociweb.grove.PiLogo.*;
import static com.ociweb.grove.DexterLogo.*;
import static com.ociweb.grove.QR.*;
import static com.ociweb.grove.Huy.*;


public class OLED_96x96Behavior implements TimeListener{
	
	private final OLED_96x96_Transducer display;
	public OLED_96x96Behavior(FogRuntime rt){
		display = OLED_96x96.newTransducer(rt.newCommandChannel()); 

	}
	@Override
	public void timeEvent(long time, int iteration) {
		
		int remainder = iteration % 6;

		switch (remainder){
			case 0:
				display.display(OCI_LOGO);
				break;
			case 1:
				display.display(GRUMPY);
				break;
			case 2:
				display.display(PI_LOGO);
				break;
			case 3:
				display.display(DEX_LOGO);
				break;
			case 4:
				display.display(HUY);
			case 5:
				display.display(OCI_LINK);
			}
		
		System.out.println("Switching to image " + remainder);
		
	}
}
```
This is an example use of the OLED 96 x 96. In the behavior class, make sure to an OLED 96x96 transducer. Also included in the class, but while not shown above, are multiple 2-demensional arrays of different images. You can generate your own 2-demensional array of a jpeg or a png file to put the image on the OLED 96x96 by going to AppTest under ./src/test/java/com/ociweb/grove/.

