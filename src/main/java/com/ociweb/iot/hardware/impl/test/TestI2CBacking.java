package com.ociweb.iot.hardware.impl.test;

import java.io.IOException;

import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.util.Appendables;

public class TestI2CBacking implements I2CBacking{

    public static final int MAX_TEST_SIZE = 2048;
    public static final int MAX_ADDRESS   = 127;
    
    public static final int MAX_BACK_BITS =  7;//127 MESSAGES
    public static final int MAX_BACK_SIZE =  1<<MAX_BACK_BITS;
    public static final int MAX_BACK_MASK =  MAX_BACK_SIZE-1;
    
    
    private long[]   lastWriteTime;
    private byte[]   lastWriteAddress;
    private byte[][] lastWriteData;
    private int[]    lastWriteLength;
    private int      lastWriteIdx;
    private int      lastWriteCount;
    
    public byte[][] responses;
    public int[] responseLengths;
        
    
    public TestI2CBacking() {
        
        lastWriteTime    = new long[MAX_BACK_SIZE];
        lastWriteAddress = new byte[MAX_BACK_SIZE];
        lastWriteData    = new byte[MAX_BACK_SIZE][];
        int i = MAX_BACK_SIZE;
        while (--i>=0) {
            lastWriteData[i] = new byte[MAX_TEST_SIZE];
        }
        
        lastWriteLength  = new int[MAX_BACK_SIZE];
        
        responses = new byte[MAX_ADDRESS][];
        responseLengths = new int[MAX_ADDRESS];
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
                
        lastWriteCount++;
        lastWriteTime[lastWriteIdx] = System.currentTimeMillis();
        lastWriteAddress[lastWriteIdx] = address;
        System.arraycopy(message, 0, lastWriteData[lastWriteIdx], 0, length);
        responseLengths[lastWriteIdx] = length;
        lastWriteIdx = MAX_BACK_MASK & lastWriteIdx++;
        
    }
    
    public void clearWriteCount() {
        lastWriteCount = 0;
    }
    

    public int getWriteCount() {
        return lastWriteCount;
    }
    
    
    public <A extends Appendable>void outputLastI2CWrite(A target, int backCount) {
                        
        try {
            int previous = MAX_BACK_MASK & ((lastWriteIdx + MAX_BACK_SIZE) - backCount);
            Appendables.appendHexDigits(target, this.lastWriteAddress[previous]).append(" ");
            Appendables.appendArray(target, '[', this.lastWriteData[previous], ']', this.lastWriteLength[previous]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }



}
