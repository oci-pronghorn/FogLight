package com.ociweb.iot.maker;

public interface I2CListener {

    public void i2cEvent(byte addr, byte[] backing, int position, int length, int mask);
    
}
