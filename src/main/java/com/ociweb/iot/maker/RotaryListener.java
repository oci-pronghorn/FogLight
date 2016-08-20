package com.ociweb.iot.maker;

public interface RotaryListener {

    public void rotaryEvent(Port port, long time, int value, int delta, int speed);
    
}
