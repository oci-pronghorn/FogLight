package com.ociweb.iot.gasPumpSimulator;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Port;

public class ModeSelector implements AnalogListener {

	private final CommandChannel commandChannel;
	private PumpState lastState;
	private int angleDivisor;

	public ModeSelector(DeviceRuntime runtime, int angleRange) {
		this.commandChannel = runtime.newCommandChannel();
		this.angleDivisor = 1+(angleRange/PumpState.values().length); //rounds up so 1024 does not produce a new state
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		
		PumpState state = PumpState.values()[value/angleDivisor];
		if (state!=lastState) {
			commandChannel.changeStateTo(state);
			lastState=state;
		}
		
	}
}
