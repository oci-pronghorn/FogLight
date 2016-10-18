package com.ociweb.iot.gasPumpSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Port;

public class ModeSelector implements AnalogListener {

	private final CommandChannel commandChannel;
	private final Logger logger = LoggerFactory.getLogger(ModeSelector.class);
	
	private int angleDivisor;
    private long lastChange; //used to keep user from toggling between states quickly

	public ModeSelector(DeviceRuntime runtime, int angleRange) {
		this.commandChannel = runtime.newCommandChannel();
		this.angleDivisor = 1+((angleRange-1)/PumpState.values().length); //rounds up so 1023 does not produce a new state
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {

		if ((time-lastChange)>200) {
			int pumpStateIndex = value/angleDivisor;
			PumpState state = PumpState.values()[pumpStateIndex];
			logger.info("changed mode to {}",state);
			commandChannel.changeStateTo(state);
			lastChange = time;
		}

	}
}
