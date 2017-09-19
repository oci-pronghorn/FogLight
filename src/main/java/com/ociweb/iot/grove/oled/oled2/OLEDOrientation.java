package com.ociweb.iot.grove.oled.oled2;

public enum OLEDOrientation {
    horizontal(0xC8),
    vertical(0xC0);

    public final int COMMAND;

    OLEDOrientation(int command){
        this.COMMAND = command;
    }
}

