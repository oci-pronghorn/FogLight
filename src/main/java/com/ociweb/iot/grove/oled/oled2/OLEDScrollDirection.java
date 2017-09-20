package com.ociweb.iot.grove.oled.oled2;

public enum OLEDScrollDirection {
    right(0x27),
    left(0x26);

    public final int COMMAND;

    OLEDScrollDirection(int command){
        this.COMMAND = command;
    }
}
