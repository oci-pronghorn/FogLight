package com.ociweb.iot.maker;

/**
 * Functional interface that can be registered with a {@link DeviceRuntime}
 * to receive a single event when the device starts.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface StartupListener {

    /**
     * Invoked once when the {@link DeviceRuntime} starts up the IoT application.
     */
    void startup();
    
}
