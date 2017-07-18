package com.ociweb.oe.foglight.api;

import com.ociweb.gl.api.StateChangeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.oe.foglight.api.StateMachine.StopLight;

public class StateChangeBehavior implements StateChangeListener {


	@Override
	public boolean stateChange(Enum oldState, Enum newState) {
		
		System.out.println("The light has chnaged to " + ((StopLight) newState).getColor());
		return true;
	}

}
