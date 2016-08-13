package com.ociweb.iot.maker;

public interface ListenerFilter {

	
	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilter includeAnalogConnections(int ... connections);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilter excludeAnalogConnections(int ... connections);
	
	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilter includeDigitalConnections(int ... connections);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilter excludeDigitalConnections(int ... connections);

	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilter includeI2CConnections(int ... connections);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilter excludeI2CConnections(int ... connections);
	
	
	/**
	 * Add subscription to this topic to this listener at startup.
	 * @param topic
	 */
	ListenerFilter addSubscription(CharSequence topic); 
	
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter includeStateChangeTo(E ... states); 

	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter excludeStateChangeTo(E ... states); 

	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter includeStateChangeFrom(E ... states); 
	
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter excludeStateChangeFrom(E ... states); 
	
	
	
}
