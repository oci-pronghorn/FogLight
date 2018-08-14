package com.ociweb.iot.grove.oled.oled2;

public enum OLEDScreenPresentation {
    normal(0xA4),
    inverted(0xA7);

    public final int COMMAND;

    OLEDScreenPresentation(int command){
        this.COMMAND = command;
    }
}
