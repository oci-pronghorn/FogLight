package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerMagDataRate {
    hz3(0x00<<2),
    hz6(0x01<<2),
    hz12(0x02<<2),
    hz25(0x03<<2),
    hz50(0x04<<2),
    hz100(0x05<<2);

    private final int specification;

    AccelerometerMagDataRate(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}

