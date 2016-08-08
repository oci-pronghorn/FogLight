package com.ociweb.iot.maker;

public interface StateChangeListener<E extends Enum<E>> {
	
	public void stateChange(E oldState, E newState);
	
}
