package com.ociweb.iot.maker;

import com.ociweb.gl.api.GreenApp;
import com.ociweb.gl.api.GreenCommandChannel;

/**
 * Base interface for a maker's IoT application.
 *
 * An implementation of this interface should be supplied
 * to {@link DeviceRuntime#run(IoTSetup)} in order to declare
 * the connections (digital, analog, etc.) and behavior (listeners)
 * that an IoT application will use during its lifecycle.
 *
 * @author Nathan Tippy
 */
public interface IoTSetup extends GreenApp<Hardware, DeviceRuntime> {
	
	public static final int ALL = GreenCommandChannel.DYNAMIC_MESSAGING | 
			                      GreenCommandChannel.NET_REQUESTER | 
			                      GreenCommandChannel.NET_RESPONDER;
	
	public static final int DYNAMIC_MESSAGING = GreenCommandChannel.DYNAMIC_MESSAGING;
	public static final int NET_REQUESTER = GreenCommandChannel.NET_REQUESTER;
	public static final int NET_RESPONDER = GreenCommandChannel.NET_RESPONDER;
	
	void declareConnections(Hardware builder);
	
	default void declareConfiguration(Hardware hardware) {
		declareConnections(hardware);
	}
}
