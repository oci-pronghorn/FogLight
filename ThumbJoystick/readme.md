# Starting a FogLighter project using Maven: 
[Instructions here.](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch reads and prints the X and Y values of the Joystick. In addition, it detects presses.

Demo code:


```java
public class ThumbJoystickBehavior implements Behavior, PressableJoystickListener{
	private ThumbJoystickTransducer tj;
	
	public ThumbJoystickBehavior(Port p){
		tj = ThumbJoystick.newTransducer(p);
		tj.registerThumbJoystickListener(this);
	}
	

	@Override
	public void joystickValues(int x, int y) {
		System.out.println("X: " + x + ", Y: " + y);
	}

	
	@Override
	public void buttonStateChange(boolean pressed, long time,long previousDuration){
		System.out.println("Pressed:" + pressed + ", Duration: " + previousDuration);
	}

}
```



The Joystick is made out of two potentiometers rotating in two orthogonal (X and Y) planes. They are physically constrained so that their values would read between around 200 to 800. When the joystick is pressed, the X value will read 1023 and can be used to detect presses.

All of that logic has already been implemented for you in the ThumbJoyStickTransducer. Initialize an instance of the ThumbJoystickTransducer, passing in the port. Keep in mind that the joystick actually takes up two "pins," hence, if one was to plug the joystick into A0. A0 is now X an A1 is now Y. Although nothing will be plugged into A1 physically, it is occupied by the joystick.

In order to listen to the JoyStick, register either a PressableJoystickListener or a ThumbJoystickListener on the transducer created. In the example, we have simply implemented the PressableJoystickListener in our Behavior class. Hence, we register "this" onto the transducer, as "this" is the ThumbJoystickBehavior class, which is also an implementation of PressableJoystickListener.

Upon a button press or release, the buttonStateChange callback method will tell us whether the new state is pressed or not, and how long the previous state was maintined. This will be useful for detecting double clicks.





