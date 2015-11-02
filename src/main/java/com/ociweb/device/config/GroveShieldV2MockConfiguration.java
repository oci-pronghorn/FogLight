package com.ociweb.device.config;

import java.util.Random;

import com.ociweb.device.grove.GroveConnect;

public class GroveShieldV2MockConfiguration extends GroveConnectionConfiguration {

    public GroveShieldV2MockConfiguration(boolean publishTime, boolean configI2C, GroveConnect[] encoderInputs,
            GroveConnect[] digitalInputs, GroveConnect[] digitalOutputs, GroveConnect[] pwmOutputs,
            GroveConnect[] analogInputs) {
        super(publishTime, configI2C, encoderInputs, digitalInputs, digitalOutputs, pwmOutputs, analogInputs);
    }
    
    Random r = new Random(101);

    @Override
    public int readBit(int connector) {
       return 1&r.nextInt();
    }

    @Override
    public int readInt(int connector) {
        return Math.abs(r.nextInt());
    }

    @Override
    public void configurePinsForDigitalInput(byte connection) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configurePinsForAnalogInput(byte connection) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configurePinsForI2C() {
        // TODO Auto-generated method stub

    }

    @Override
    public void i2cSetClockLow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void i2cSetClockHigh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void i2cSetDataLow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void i2cSetDataHigh() {
        // TODO Auto-generated method stub

    }

    @Override
    public int i2cReadData() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int i2cReadClock() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void i2cDataIn() {
        // TODO Auto-generated method stub

    }

    @Override
    public void i2cDataOut() {
        // TODO Auto-generated method stub

    }

    @Override
    public void coldSetup() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cleanup() {
        // TODO Auto-generated method stub

    }

}
