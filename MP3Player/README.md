# Hardware Needed
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)

# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch demonstrates a simple application to control two LEDs using an Angle Sensor.

Demo code (copy and paste this to a FogLighter project):
First declare the connections in MP3Player.java:


```java
package com.ociweb.grove;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class MP3Player implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////


    @Override
    public void declareConnections(Hardware c) {
    	c.useSerial(Baud.B_____9600);
    	c.setTimerPulseRate(1000);
    	//c.enableTelemetry();
    	c.limitThreads();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.registerListener(new MP3Behavior(runtime));
    	runtime.registerListener(new MonitoringBehavior());
    }
    public static void main (String[] args){
    	FogRuntime.run(new MP3Player());
    }
          
}
```


Then specify the behavior of the program in the Behavior class:

```java
package com.ociweb.grove;

import static com.ociweb.iot.grove.mp3_v2.MP3_V2_Consts.AudioStorageDevice.SD_Card;
import static com.ociweb.iot.maker.FogCommandChannel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.mp3_v2.MP3_V2_Transducer;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.util.Appendables;


public class MP3Behavior implements Behavior, StartupListener{
	Logger logger = LoggerFactory.getLogger(MP3Behavior.class);
	
	private final MP3_V2_Transducer s;

	public MP3Behavior(FogRuntime rt){
		s = new MP3_V2_Transducer(rt.newCommandChannel(SERIAL_WRITER));//SERIAL_WRITER));
	}

	@Override
	public void startup() {
		logger.warn("Selecting device {} ",s.selectDevice(SD_Card));
		logger.warn("Setting volume {}",s.setVolume(28));
		logger.warn("Playing song in MP3 folder {}" , s.playSongInMP3Folder(0001));
		logger.warn("Resuming {}" ,s.resume());
		
	}
}
```


```java
package com.ociweb.grove;

import com.ociweb.iot.maker.SerialListener;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class MonitoringBehavior implements SerialListener {

	@Override
	public int message(ChannelReader reader) {
		if (reader.available() > 0){
			int consumed = 0;
			while (reader.hasRemainingBytes()){
				System.out.print("Input:" + Integer.toHexString( reader.read()));
				consumed++;
			}
			System.out.println();
			return consumed;
		}
		return 0;
	}
	
}
```


When executed, turning the knob will cause LED2 to turn on/off and LED1 to fade in/out accordingly. 




