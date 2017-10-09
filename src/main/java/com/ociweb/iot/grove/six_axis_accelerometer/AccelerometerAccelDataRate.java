package com.ociweb.iot.grove.six_axis_accelerometer;

/**
 * Set accelerometer output data rate
 * @param aRate
 * 1 = 3Hz, 2 = 6Hz, 3 =12 Hz,  4 = 25 Hz, 5 = 50Hz
 * 6 = 100 Hz, 7 = 200 Hz, 8 = 400 Hz
 * 9 = 800 Hz, 10 = 1600 Hz
 */

public enum AccelerometerAccelDataRate {
    hz3(1),
    hz6(2),
    hz12(3),
    hz25(4),
    hz50(5),
    hz100(6),
    hz200(7),
    hz400(8),
    hz800(9),
    hz1600(10);

    private final int specification;

    AccelerometerAccelDataRate(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}
