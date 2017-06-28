package com.ociweb.grove;
import com.ociweb.iot.maker.*;


public class OLED128x64 implements FogApp
{
	@Override
	public void declareConnections(Hardware c) {
		c.useI2C();
		c.setTriggerRate(125);
		//c.enableTelemetry(true);
		//TODO: give warning message if trigger rate was not set and time listener is used
	}

	public static void main (String[] args){
		FogRuntime.run(new OLED128x64());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel(DYNAMIC_MESSAGING,20000);
		GameOfLife game = new GameOfLife(ch);
		runtime.addListener(game);
	}
}
