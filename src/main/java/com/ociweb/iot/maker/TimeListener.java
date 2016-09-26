package com.ociweb.iot.maker;

/**
 * Functional interface for a listener for time events triggered
 * by the {@link DeviceRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface TimeListener {

    /**
     * Invoked when a time event is received from the {@link DeviceRuntime}.
     *
     * @param time Time of the event in milliseconds since the UNIX epoch.
     */
    void timeEvent(long time);
}
