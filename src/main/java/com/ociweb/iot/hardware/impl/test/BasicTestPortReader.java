package com.ociweb.iot.hardware.impl.test;

import com.ociweb.iot.maker.Port;

public class BasicTestPortReader implements TestPortReader {
    @Override
    public int read(Port port, int pinData, int hardwareRange) {
        return pinData + (port.isAnalog() ? (Math.random()<.1 ? 1 : 0) : 0); //adding noise for analog values
    }
}
