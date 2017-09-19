package com.ociweb.iot.grove.oled.oled2;

public enum OLEDScrollSpeed {
    frames2(0x07),
    frames3(0x04),
    frames4(0x05),
    frames5(0x00),
    frames25(0x06),
    frames64(0x01),
    frames128(0x02),
    frames256(0x03);

    public final int COMMAND;

    OLEDScrollSpeed(int command){
        this.COMMAND = command;
    }
}
