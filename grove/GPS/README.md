# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to identify GPS coordinates of a sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in GPS.java:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class GPS implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {
    	 c.useSerial(Baud.B_____9600);
    	 c.limitThreads();
    	 
    }
    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new GPSBehavior(runtime));
    }
          
}
```


Then specify the behavior of the program in the GPSBehavior class:

```java
package com.ociweb.grove;

import static com.ociweb.iot.maker.FogRuntime.SERIAL_WRITER;

import com.ociweb.iot.grove.gps.GPS_Transducer;
import com.ociweb.iot.grove.gps.GeoCoordinateListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class GPSBehavior implements GeoCoordinateListener {
	private FogCommandChannel ch;
	private GPS_Transducer gps;
	public GPSBehavior(FogRuntime rt){
		this.gps = new GPS_Transducer(rt.newCommandChannel(SERIAL_WRITER), this);

	}
	@Override
	public void coordinates(int longtitude, int lattitude) {
		System.out.println("Long:" + longtitude + ", Lat: " + lattitude);
	}
	
}
```


When executed, the GPS sensor will return longitude and latitude values of the location of the sensor back to the executor.




