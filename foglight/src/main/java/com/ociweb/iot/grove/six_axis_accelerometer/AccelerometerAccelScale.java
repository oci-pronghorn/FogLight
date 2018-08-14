package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerAccelScale {
    gauss2(0x00<<3),
    gauss4(0x01<<3),
    gauss6(0x02<<3),
    gauss8(0x03<<3),
    gauss16(0x04<<3);

    private final int specification;

    AccelerometerAccelScale(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}
