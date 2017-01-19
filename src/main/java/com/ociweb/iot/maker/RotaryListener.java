package com.ociweb.iot.maker;

/**
 * Functional interface for a listener that receives rotary events from
 * encoders registered with a {@link DeviceRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface RotaryListener {

    /**
     * Invoked when the {@link DeviceRuntime} detects a change in the state
     * of a registered rotary encoder.
     *
     * @param port {@link Port} of the rotary encoder whose state changed.
     * @param time UNIX timestamp (milliseconds since the epoch) of when this event was received.
     * @param value Current value of the rotary encoder.
     * @param delta Difference since the last value of the rotary encoder.
     * @param speed Estimated speed in TODO: units of the rotary encoder.
     */
    public void rotaryEvent(Port port, long time, int value, int delta, int speed);
    
}
