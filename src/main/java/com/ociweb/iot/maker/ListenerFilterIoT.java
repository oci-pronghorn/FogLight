package com.ociweb.iot.maker;

import com.ociweb.gl.api.ListenerFilter;

public interface ListenerFilterIoT extends ListenerFilter {

	
	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilterIoT includePorts(Port ... ports);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilterIoT excludePorts(Port ... ports);

	/**
	 * Only the listed connections.
	 * @param connections
	 */
	ListenerFilterIoT includeI2CConnections(int ... addresses);
	
	/**
	 * All known connections except for those listed.
	 * @param connections
	 */
	ListenerFilterIoT excludeI2CConnections(int ... addresses);

	
}
