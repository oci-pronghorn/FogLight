package com.ociweb.iot.impl;

public interface I2CListenerBase {
	   public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask);
}
