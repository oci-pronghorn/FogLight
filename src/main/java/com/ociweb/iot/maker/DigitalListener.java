package com.ociweb.iot.maker;

public interface DigitalListener {

    public void digitalEvent(Port port, long time, long durationMillis, int value);
    
}
