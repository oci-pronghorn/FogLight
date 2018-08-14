package com.ociweb.iot.impl;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public interface RotaryListenerBase {

    /**
     * Invoked when the {@link FogRuntime} detects a change in the state
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
