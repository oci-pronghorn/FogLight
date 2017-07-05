package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;

public interface DigitalListener extends Behavior {

    public void digitalEvent(Port port, long time, long durationMillis, int value);
    
}
