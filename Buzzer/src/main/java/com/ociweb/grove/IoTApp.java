package com.ociweb.grove;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.GroveTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    //private static final Port BUTTON_PORT = D3;
    //private static final Port RELAY_PORT  = D7;
    //private static final Port BUZZER_PORT = D8;
    private static final Port SERVO_PORT = A2;

    @Override
    public void declareConnections(Hardware c) {
        //c.connect(Button, BUTTON_PORT); 
        //c.connect(Relay, RELAY_PORT);         
        //c.connect(Buzzer, BUZZER_PORT);
        c.connect(Servo, SERVO_PORT);
        c.setTriggerRate(20);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
  
        final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        //runtime.addDigitalListener((port, connection, time, value)->{ 
    	    //channel1.setValueAndBlock(BUZZER_PORT, value == 1, 500);//500 is the time in milliseconds that any
	    //channel1.setValueAndBlock(RELAY_PORT, value ==1, 500);  //further action is blocked 
    	   ///});
        //runtime.addTimeListener((time, instance)->
    }
}
