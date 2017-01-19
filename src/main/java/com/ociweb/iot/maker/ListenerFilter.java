package com.ociweb.iot.maker;

public interface ListenerFilter {

	
	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilter includePorts(Port ... ports);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilter excludePorts(Port ... ports);

	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilter includeI2CConnections(int ... addresses);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilter excludeI2CConnections(int ... addresses);
	
	
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
	<E extends Enum<E>> ListenerFilter includeStateChangeToAndFrom(E ... states); 
	
	
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter excludeStateChangeFrom(E ... states); 
	
	
	
}
