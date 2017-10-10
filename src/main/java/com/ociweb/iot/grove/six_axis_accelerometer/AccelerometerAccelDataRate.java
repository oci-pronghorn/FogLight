package com.ociweb.iot.grove.six_axis_accelerometer;

/**
 * Set accelerometer output data rate
 * @param aRate
 * 1 = 3Hz, 2 = 6Hz, 3 =12 Hz,  4 = 25 Hz, 5 = 50Hz
 * 6 = 100 Hz, 7 = 200 Hz, 8 = 400 Hz
 * 9 = 800 Hz, 10 = 1600 Hz
 */

public enum AccelerometerAccelDataRate {
    hz3(0x01<<4),
    hz6(0x02<<4),
    hz12(0x03<<4),
    hz25(0x04<<4),
    hz50(0x05<<4),
    hz100(0x06<<4),
    hz200(0x07<<4),
    hz400(0x08<<4),
    hz800(0x09<<4),
    hz1600(0x0A<<4);

    private final int specification;

    AccelerometerAccelDataRate(int specification) {
        this.specification = specification;
    }

    int getSpecification() {
        return specification;
    }
}

