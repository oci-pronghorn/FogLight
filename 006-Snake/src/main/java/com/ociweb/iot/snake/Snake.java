package com.ociweb.iot.snake;


import static com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Snake implements FogApp
{


    @Override
    public void declareConnections(Hardware c) {
       c.connect(ThumbJoystick, A0);
       c.setTimerPulseRate(1000);
       c.useI2C();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new SnakeBehavior(runtime));
    }
          
}
