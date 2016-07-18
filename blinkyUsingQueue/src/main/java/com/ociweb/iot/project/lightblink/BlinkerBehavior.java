package com.ociweb.iot.project.lightblink;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.TimeListener;

public class BlinkerBehavior implements TimeListener {

	private int state = 0;
	private CommandChannel commandChannel;
    private static final int PAUSE = 500;
	
	public BlinkerBehavior(IOTDeviceRuntime runtime) {
		commandChannel = runtime.newCommandChannel();
	}
	
	@Override
	public void timeEvent(long arg0) {
		
		//keep adding commands if more can be accepted
		while (commandChannel.digitalSetValueAndBlock(IoTApp.LED_CONNECTION, state, PAUSE)) {
   		 	state = (1==state ? 0 : 1);
   	    }   	
		
	}

}
