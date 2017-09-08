package com.ociweb.iot.hardware.impl.test;

import com.ociweb.iot.maker.Port;

public class SinTestPortReader implements TestPortReader {
    private long iteration =  360;
    @Override
    public int read(Port port, int pinData, int hardwareRange) {long degrees = iteration % 360;
        double radians = degrees * Math.PI / 180.0;
        double s = (Math.sin(radians) + 1.0) / 2.0;
        long value = Math.round(hardwareRange * s);
        iteration++;
        return (int) value;
    }
}
