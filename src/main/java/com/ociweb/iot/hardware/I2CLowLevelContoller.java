package com.ociweb.iot.hardware;

public interface I2CLowLevelContoller {

    
    public void setup();
    
    public void i2cSetClockLow();
    public void i2cSetClockHigh();
    public void i2cSetDataLow();
    public void i2cSetDataHigh();
    public int i2cReadData();
    public int i2cReadClock();
    
    public void i2cDataIn();
    public void i2cDataOut();
    public void i2cClockIn();
    public void i2cClockOut();
    public boolean i2cReadAck();
    public boolean i2cReadClockBool();
    public boolean i2cReadDataBool();

    public void debugI2CRateLastTime(long l);

    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend);
    
    
}