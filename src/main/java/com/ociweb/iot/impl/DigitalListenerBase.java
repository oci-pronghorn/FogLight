package com.ociweb.iot.impl;

import com.ociweb.iot.maker.Port;

public interface DigitalListenerBase {
    public void digitalEvent(Port port, long time, long durationMillis, int value);
}
