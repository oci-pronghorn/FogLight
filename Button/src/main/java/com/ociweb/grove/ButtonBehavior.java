package com.ociweb.grove;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class ButtonBehavior implements DigitalListener {

	private static final Port RELAY_PORT = D7;
	
    final FogCommandChannel channel1;
	public ButtonBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
       channel1 = runtime.newCommandChannel();

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
        channel1.setValueAndBlock(RELAY_PORT, value == 1, 500); //500 is the amount of time in milliseconds that                                                                                         //delays a future action

	}

}
