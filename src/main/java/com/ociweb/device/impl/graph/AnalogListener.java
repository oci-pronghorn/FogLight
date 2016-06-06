package com.ociweb.device.impl.graph;

public interface AnalogListener {

    public void analogEvent(int equip, int connector, int average, int value);
    
}
