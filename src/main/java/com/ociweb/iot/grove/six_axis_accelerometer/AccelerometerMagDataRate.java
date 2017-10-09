package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerMagDataRate {
    hz3(0),
    hz6(1),
    hz12(2),
    hz25(3),
    hz50(4);

    private final int specification;

    AccelerometerMagDataRate(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}

