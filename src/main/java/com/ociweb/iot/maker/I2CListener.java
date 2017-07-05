package com.ociweb.iot.maker;

import com.ociweb.gl.api.Behavior;

public interface I2CListener extends Behavior {

    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask);
    
}
