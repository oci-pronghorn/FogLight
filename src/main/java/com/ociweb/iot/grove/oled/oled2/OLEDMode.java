package com.ociweb.iot.grove.oled.oled2;

enum OLEDMode {
    instruction(0x80),
    data(0x40);

    public final int COMMAND;

    OLEDMode(int command) {
        this.COMMAND = command;
    }
}
