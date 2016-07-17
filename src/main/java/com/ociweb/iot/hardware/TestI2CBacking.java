package com.ociweb.iot.hardware;

import com.ociweb.pronghorn.iot.i2c.I2CBacking;

public class TestI2CBacking implements I2CBacking {

    @Override
    public byte[] read(byte address, byte[] target, int bufferSize) {
        // TODO Auto-generated method stub
        
        //what is the right read
        
        return null;
    }

    @Override
    public void write(byte address, byte[] message, int length) {
        // TODO Auto-generated method stub
   
        //store message but how can we know that the response is?
        
    }
    

}
