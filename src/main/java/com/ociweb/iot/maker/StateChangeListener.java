package com.ociweb.iot.maker;

/**
 * Functional interface for changes in a state machine registered with the
 * {@link FogRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface StateChangeListener<E extends Enum<E>> {

	/**
	 * Invoked when a state machine registered with the {@link FogRuntime}
	 * changes state.
	 *
	 * @param oldState Old state of the state machine.
	 * @param newState New state of the state machine.
	 */
	void stateChange(E oldState, E newState);
	
}
