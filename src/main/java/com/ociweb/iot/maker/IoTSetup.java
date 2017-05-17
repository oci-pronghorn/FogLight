package com.ociweb.iot.maker;

import com.ociweb.gl.api.GreenApp;

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

	void declareConnections(Hardware builder);
	
	default void declareConfiguration(Hardware hardware) {
		declareConnections(hardware);
	}
}
