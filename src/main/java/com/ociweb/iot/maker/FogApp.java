package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgApp;
import com.ociweb.gl.api.GreenCommandChannel;

/**
 * Base interface for a maker's IoT application.
 *
 * An implementation of this interface should be supplied
 * to {@link FogRuntime#run(FogApp)} in order to declare
 * the connections (digital, analog, etc.) and behavior (listeners)
 * that an IoT application will use during its lifecycle.
 *
 * @author Nathan Tippy
 */
public interface FogApp extends MsgApp<Hardware, FogRuntime> {
	
	
	public static final int DYNAMIC_MESSAGING = GreenCommandChannel.DYNAMIC_MESSAGING;
	public static final int NET_REQUESTER     = GreenCommandChannel.NET_REQUESTER;
	public static final int NET_RESPONDER     = GreenCommandChannel.NET_RESPONDER;
	
	public static final int I2C_WRITER        = FogCommandChannel.I2C_WRITER;
	public static final int PIN_WRITER        = FogCommandChannel.PIN_WRITER;
	public static final int SERIAL_WRITER     = FogCommandChannel.SERIAL_WRITER;
	public static final int BT_WRITER         = FogCommandChannel.BT_WRITER;
	
	public static final int ALL = DYNAMIC_MESSAGING | 
								  NET_REQUESTER | 
								  NET_RESPONDER |
								  I2C_WRITER |
								  PIN_WRITER |
								  SERIAL_WRITER |
								  BT_WRITER;
	
	void declareConnections(Hardware builder);
	
	default void declareConfiguration(Hardware hardware) {
		declareConnections(hardware);
	}
}
