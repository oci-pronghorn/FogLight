package com.ociweb.device.impl.grovepi;

import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveTwig;

/**
 * TODO: GPIO 03 is SDA on an RPi
 *       GPIO 05 is SCL on an RPi
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiConstants {
    
    public static final short[] GPIO_PINS = new short[] {
        3,
        5,
        7,
        11,
        12,
        13,
        15,
        16,
        18,
        22,
        29,
        31,
        32,
        33,
        35,
        36,
        37,
        38,
        40
    };

    public static final short[] GPIO_PIN_MODES = new short[] {
        3,
        5,
        7,
        11,
        12,
        13,
        15,
        16,
        18,
        22,
        29,
        31,
        32,
        33,
        35,
        36,
        37,
        38,
        40
    };

    public static final short[] OUTPUT_ENABLE = new short[] {
        3,
        5,
        7,
        11,
        12,
        13,
        15,
        16,
        18,
        22,
        29,
        31,
        32,
        33,
        35,
        36,
        37,
        38,
        40
    };
    
    public static final short[] PULL_UP_ENABLE = new short[] {
        3,
        5,
        7,
        11,
        12,
        13,
        15,
        16,
        18,
        22,
        29,
        31,
        32,
        33,
        35,
        36,
        37,
        38,
        40
    };
    
    public static final GroveConnect[] i2cPins = new GroveConnect[] {
    new GroveConnect(GroveTwig.I2C, 0),
    new GroveConnect(GroveTwig.I2C, 1)};
    
    public final static int DATA_RAW_VOLTAGE = 0;
    public final static int CLOCK_RAW_VOLTAGE = 1;
    public final static int HIGH_LINE_VOLTAGE_MARK = 1 << 8; //This is a number needing 9 or more full bits.
}
