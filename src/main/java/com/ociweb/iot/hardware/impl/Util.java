package com.ociweb.iot.hardware.impl;

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

    public static byte[] rotaryMap = new byte[255];
    
    static {
        
        Util.rotaryMap[0b01001011] = -1;
        
        Util.rotaryMap[0b11001011] = -1;
        Util.rotaryMap[0b11011011] = -1;
        Util.rotaryMap[0b11010011] = -1;
        Util.rotaryMap[0b10111011] = -1;//fast spin check
        
        Util.rotaryMap[0b10000111] = 1;
        
        Util.rotaryMap[0b11000111] = 1;
        Util.rotaryMap[0b11100111] = 1;
        Util.rotaryMap[0b11100011] = 1;
        Util.rotaryMap[0b01110111] = 1;//fast spin check
        Util.rotaryMap[0b01000111] = 1;//fast spin check

    }
    
}
