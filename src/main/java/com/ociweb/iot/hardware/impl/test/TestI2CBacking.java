package com.ociweb.iot.hardware.impl.test;

import com.ociweb.pronghorn.iot.i2c.I2CBacking;

public class TestI2CBacking implements I2CBacking{

    public static final int MAX_TEST_SIZE = 4096;
    public static final int MAX_ADDRESS = 127;
    
    public long lastWriteTime;
    public byte lastWriteAddress;
    public byte[] lastWriteData;
    public int lastWriteLength;
    
    
    public byte[][] responses;
    public int[] responseLengths;
        
    
    public TestI2CBacking() {
        
        lastWriteData = new byte[MAX_TEST_SIZE];
        responses = new byte[MAX_ADDRESS][];
    }
    
    
    public void setValuetoRead(byte address, byte[] data, int length) {
        responses[address] = data;
        responseLengths[address] = length;
    }
        
    
    @Override
    public byte[] read(byte address, byte[] target, int length) {        
        System.arraycopy(responses[address], 0, target, 0, Math.min(length, responseLengths[address]));
        return target;
        
    }

    @Override
    public void write(byte address, byte[] message, int length) {
        lastWriteTime = System.currentTimeMillis();
        lastWriteAddress = address;
        System.arraycopy(message, 0, lastWriteData, 0, length);
        responseLengths[address] = length;
    }

}
