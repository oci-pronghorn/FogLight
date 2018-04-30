package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.PubSubService;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.oe.foglight.api.StateMachine.StopLight;

public class TimingBehavior implements TimeListener {
	private static long startTime;
	private static boolean haveStartTime = false;
	private static final long fullTime = 15; //time from one red light to the next in milliseconds
	private final PubSubService pubSubService;


	public TimingBehavior(FogRuntime runtime) {
		FogCommandChannel channel = runtime.newCommandChannel();
		pubSubService = channel.newPubSubService();
	}


	@Override
	public void timeEvent(long time, int iteration) {
		
		if((time-startTime)%fullTime == 5) {
			System.out.print("Go! ");
			pubSubService.changeStateTo(StopLight.Go);
		}
		else if((time-startTime)%fullTime == 10) {
			System.out.print("Caution. ");
			pubSubService.changeStateTo(StopLight.Caution);
		}
		else if((time-startTime)%fullTime == 0) {
			System.out.print("Stop! ");
			pubSubService.changeStateTo(StopLight.Stop);
		}
		
		if(!haveStartTime) {
			startTime = time;
			haveStartTime = true;
		}
	}

}
