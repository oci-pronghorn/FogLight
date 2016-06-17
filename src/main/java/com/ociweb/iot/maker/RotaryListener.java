package com.ociweb.iot.maker;

public interface RotaryListener {

    public void rotaryEvent(int connector, int value, int delta, int speed);
    
}
