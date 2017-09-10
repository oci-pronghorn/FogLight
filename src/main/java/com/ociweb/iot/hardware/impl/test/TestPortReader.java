package com.ociweb.iot.hardware.impl.test;

import com.ociweb.iot.maker.Port;

public interface TestPortReader {
    int read(Port port, int pinData, int hardwareRange);
}
