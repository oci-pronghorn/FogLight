package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerMagRes {
    low(0x00<<5),
    high(0x03<<5);

    private final int specification;

    AccelerometerMagRes(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }

}
