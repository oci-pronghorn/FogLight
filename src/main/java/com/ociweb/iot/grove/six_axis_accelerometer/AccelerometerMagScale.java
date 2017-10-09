package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerMagScale {
    gauss2(1),
    gauss4(2),
    gauss8(3),
    gauss12(4);

    private final int specification;

    AccelerometerMagScale(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}

