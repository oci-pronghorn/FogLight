package com.ociweb.iot.maker;

public interface AnalogListener {

    public void analogEvent(int connector, long time, int average, int value);
    
}
