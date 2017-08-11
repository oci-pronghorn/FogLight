package com.ociweb.iot.snake;


import static com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Snake implements FogApp
{

	final Port joystickPort = A0;

    @Override
    public void declareConnections(Hardware c) {
       c.connect(ThumbJoystick, joystickPort);
       c.setTimerPulseRate(250);
       c.useI2C();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new SnakeBehavior(runtime, joystickPort));
    }
          
}
