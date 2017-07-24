# OE FogLight Grove Examples
FogLight is a lightweight runtime that enables makers of all ages and skill levels to create highly performant apps for embedded devices like Raspberry Pis.

## What It Is ##
FogLight is a Java 8 functional API for embedded systems that's built on top of [GreenLightning](https://github.com/oci-pronghorn/GreenLightning), a small footprint, garbage free compact 1 Java web server and message routing platform, 

FogLight is...
- Fast - Built on top of GreenLightning, FogLight is a garbage-free, lock-free and low latency way to talk directly to hardware.
- Simple - Taking advantage of the latest Java 8 APIs, FogLight has a clean and fluent set of APIs that make it easy to learn and apply with minimal training.
- Secure - By taking advantage of the compile-time graph validation system, all FogLight applications can be compiled and compressed to a point where injecting malicious code into the final production JAR would prove difficult, if not impossible.

## How It Works ##
Every FogLight application starts with an `FogApp` implementation which initializes the `FogRuntime` by defining various hardware connections and behaviors for handling state changes in those connections.  

A very simple example of a FogLight application is below (omitting boilerplate import statements and so on); this app makes an LED connected to a GrovePi board blink every 500 milliseconds:
```
public class IoTApp implements FogApp {
    
    private static final int PAUSE = 500;
           
    public static final Port LED_PORT = D5;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
        c.setTriggerRate(PAUSE*2);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel blinkerChannel = runtime.newCommandChannel(); 
        
        runtime.addTimeListener((time,instance)->{
        	
        	blinkerChannel.setValueAndBlock(LED_PORT, true, PAUSE);
        	blinkerChannel.setValue(LED_PORT, false);
        });
    }  
}
``` 
Of course, this is just a simple example; for more interesting examples that demonstrate the various features currently available in FogLight, you should take a look at [our examples repository](#information-and-demos-for-grove-devices).
## What You Need Befor You Start:
### Hardware
- [Raspberry Pi](https://www.raspberrypi.org/)
- [GrovePi+ Board](https://www.dexterindustries.com/shop/grovepi-board/)
### Software
- [Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- [Maven](https://maven.apache.org/install.html)
- [Git](https://git-scm.com/)
## Starting Your Maven Project
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
## Information and Demos for Grove Devices
- Analog TollC
  - [wiki](http://wiki.seeed.cc/Grove-I2C_ADC/)
  - demo - Coming Soon!
- Angle Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Rotary_Angle_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/AngleSensor/AngleSensor.md)
- Button
  - [wiki](http://wiki.seeed.cc/Grove-Button/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/Button/Button.md)
- Buzzer
  - [wiki](http://wiki.seeed.cc/Grove-Buzzer/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/Buzzer/Buzzer.md)
- Four Digit Display
  - [wiki](http://wiki.seeed.cc/Grove-4-Digit_Display/)
  - demo - Coming Soon!
- GPS
  - [wiki](http://wiki.seeed.cc/Grove-GPS/)
  - demo - Coming Soon!
- I2C Mini Motor Driver
  - [wiki](http://wiki.seeed.cc/Grove-Mini_I2C_Motor_Driver_v1.0/)
  - demo - Coming Soon!
- I2C Motor Driver
  - [wiki](http://wiki.seeed.cc/Grove-I2C_Motor_Driver_V1.3/)
  - demo - Coming Soon!
- LCD RGB Backlight
  - [wiki](http://wiki.seeed.cc/Grove-LCD_RGB_Backlight/)
  - demo - Coming Soon!
- LED
  - [wiki](http://wiki.seeed.cc/Grove-LED_Socket_Kit/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/LED/LED.md)
- Light Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Light_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/LightSensor/LightSensor.md)
- Line Finder
  - [wiki](http://wiki.seeed.cc/Grove-Line_Finder/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/LineFinder/LineFinder.md)
- MP3Player
  - [wiki](http://wiki.seeed.cc/Grove-Serial_MP3_Player/)
  - demo - Coming Soon!
- Moisture Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Moisture_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/MoistureSensor/MoistureSensor.md)
- Motion Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Motion_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/MotionSensor/MotionSensor.md)
- OLED 128x64
  - [wiki](http://wiki.seeed.cc/Grove-OLED_Display_0.96inch/)
  - demo - Coming Soon!
- OLED 96x96
  - [wiki](http://wiki.seeed.cc/Grove-OLED_Display_1.12inch/)
  - demo - Coming Soon!
- Real Time Clock
  - [wiki](http://wiki.seeed.cc/Grove-RTC/)
  - demo - Coming Soon!
- Relay
  - [wiki](http://wiki.seeed.cc/Grove-Relay/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/Relay/Relay.md)
- Sense Hat
  - [wiki](https://pinout.xyz/pinout/sense_hat)
  - demo - Coming Soon!
- Serial Bluetooth
  - [wiki](http://wiki.seeed.cc/Grove-Serial_Bluetooth_v3.0/)
  - demo - Coming Soon!
- SixAxisAccelerometer
  - [wiki](http://wiki.seeed.cc/Grove-6-Axis_AccelerometerAndCompass_V2.0/)
  - demo -Coming Soon!
- Temperature Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Temperature_Sensor/)
  - demo - Coming Soon!
- Three Axis Acceleromter
  - [wiki](http://wiki.seeed.cc/Grove-3-Axis_Digital_Accelerometer-1.5g/)
  - demo - Coming Soon!
- Thumb Joystick
  - [wiki](http://wiki.seeed.cc/Grove-Thumb_Joystick/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/ThumbJoystick/readme.md)
- Touch Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Touch_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/TouchSensor/TouchSensor.md)
- UV Sensor
  - [wiki](http://wiki.seeed.cc/Grove-UV_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/UVSensor/UVSensor.md)
- Ultrasonic Rangefinder
  - [wiki](http://wiki.seeed.cc/Grove-Ultrasonic_Ranger/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/UltrasonicRangefinder/UltrasonicRangefinder.md)
- Variable Color LED
  - [wiki](http://wiki.seeed.cc/Grove-Variable_Color_LED/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/VariableColorLED/VariableColorLED.md)
- Vibration Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Piezo_Vibration_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/VibrationSensor/readme.md)
- Water Sensor
  - [wiki](http://wiki.seeed.cc/Grove-Water_Sensor/)
  - [demo](https://github.com/oci-pronghorn/FogLight-Grove/blob/master/WaterSensor/readme.md)
