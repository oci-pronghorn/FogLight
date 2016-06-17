package com.ociweb.iot.maker;

public interface AnalogListener {

    public void analogEvent(int connector, int average, int value);
    
}
