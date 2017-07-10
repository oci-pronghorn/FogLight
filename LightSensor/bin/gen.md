# Starting your Maven project: 
[Instructions here](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)
# Example Project:
The following sketch demonstrates a simple application using the Light Sensor: when the light sensor detects a dark enough room, the LED light will turn on, otherwise the LED will stay off.
    Demo code:
```
.include "./src/main/java/com/ociweb/grove/IoTApp.java"
```
When executed, the above code will cause the light sensor on A2 (analog output 2) to continuously print out the value of light present in the room. If the value drops below the darkValue (350), the LED light on D2 (digatal output 2) will turn on. 
