package com.ociweb.iot.grove.six_axis_accelerometer;

public enum AccelerometerMagScale {
    gauss2(0x00<<5),
    gauss4(0x01<<5),
    gauss8(0x02<<5),
    gauss12(0x03<<5);

    private final int specification;

    AccelerometerMagScale(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}

