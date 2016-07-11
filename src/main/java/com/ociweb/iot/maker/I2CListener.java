package com.ociweb.iot.maker;

public interface I2CListener {

    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask);
    
}
