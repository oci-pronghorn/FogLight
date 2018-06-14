package com.ociweb.iot.project.lightblink;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

public class BlinkerBehavior implements TimeListener {

	private boolean state;
	private FogCommandChannel commandChannel;
    private static final int PAUSE = 500;
    
	public BlinkerBehavior(FogRuntime runtime) {
		commandChannel = runtime.newCommandChannel(
				 FogRuntime.PIN_WRITER |
				MsgCommandChannel.DYNAMIC_MESSAGING);
	}
	
	@Override
	public void timeEvent(long arg0, int iteration) {
		
		//keep adding commands if more can be accepted
		while (commandChannel.setValueAndBlock(IoTApp.LED_PORT, state, PAUSE)) {
   		 	state = !state;
   	    }   	
		
	}

}
