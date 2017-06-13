package com.ociweb.iot.project.lightblink;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;

public class BlinkerBehavior implements TimeListener {

	private boolean state = false;
	private CommandChannel commandChannel;
    private static final int PAUSE = 500;
    
	public BlinkerBehavior(DeviceRuntime runtime) {
		commandChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
	}
	
	@Override
	public void timeEvent(long arg0) {
		
		//keep adding commands if more can be accepted
		while (commandChannel.setValueAndBlock(IoTApp.LED_PORT, state, PAUSE)) {
   		 	state = !state;
   	    }   	
		
	}

}
