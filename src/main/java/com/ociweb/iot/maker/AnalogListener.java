package com.ociweb.iot.maker;

public interface AnalogListener {

    public void analogEvent(int connector, long time, long durationMillis, int average, int value);
    
}
