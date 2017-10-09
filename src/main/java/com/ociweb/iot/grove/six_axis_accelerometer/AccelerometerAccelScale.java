package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerAccelScale {
    gauss2(0),
    gauss4(1),
    gauss6(2),
    gauss8(3),
    gauss16(4);

    private final int specification;

    AccelerometerAccelScale(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}
