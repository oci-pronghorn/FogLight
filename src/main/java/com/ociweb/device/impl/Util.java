package com.ociweb.device.impl;

public class Util {

    //reverse the low 4 bits and drop the high 4 bits
    //this allows for an even distribution of values filling each spot once
    public static byte reverseBits(byte bits) {
        //could use Integer.reverse but we only have 4 bits...
        return (byte) ( (0x08&(bits<<3))|
                        (0x04&(bits<<1))|
                        (0x02&(bits>>1))|
                        (0x01&(bits>>3))  );
        
    }
    
    
}
