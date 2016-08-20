package com.ociweb.iot.maker;

public interface AnalogListener {

    public void analogEvent(Port port, long time, long durationMillis, int average, int value);
    
}
