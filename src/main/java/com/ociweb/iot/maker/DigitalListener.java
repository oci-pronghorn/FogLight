package com.ociweb.iot.maker;

public interface DigitalListener {

    public void digitalEvent(int connector, long time, int value);
    
}
