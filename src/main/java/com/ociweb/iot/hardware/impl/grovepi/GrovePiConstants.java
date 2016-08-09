package com.ociweb.iot.hardware.impl.grovepi;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardwareConnection;

/**
 * TODO: GPIO 2 is SDA on an RPi
 *       GPIO 3 is SCL on an RPi
 *
 * TODO: Only pins we're actually using/care about are 2 and 3.
 * 
 * @author Brandon Sanders [brandon@alicorn.io]
 * @author Alex Herriott
 */
public class GrovePiConstants {
    
    public static final byte START_BYTE = 0x01;
    public static final byte DIGITAL_READ = 0x01;
    public static final byte DIGITAL_WRITE = 0x02;
    public static final byte ANALOG_READ = 0x03;
    public static final byte ANALOG_WRITE = 0x04;
    public static final byte PIN_MODE = 0x05;
    public static final byte OUTPUT = 0x01;
    public static final byte INPUT = 0x00;
	
	public static final short[] GPIO_PINS = new short[] {
        2,
        3,
//        5,
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
        2,
        3,
//        5,
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
        2,
        3,
//        5,
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
        2,
        3,
//        5,
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
    
    public static final int[] DIGITAL_PIN_TO_REGISTER = new int[] {
    		0,
    		1,
    		2,
    		3,
    		4,
    		5,
    		6,
    		7,
    		8,
    };
    
    public static final int[] ANALOG_PIN_TO_REGISTER = new int[] {
    		14,
    		15,
    		16	
    };
    
    public static final int[] REGISTER_TO_PIN = new int[] {
    		0,
    		1,
    		2,
    		3,
    		4,
    		5,
    		6,
    		7,
    		8,
    		-1,
    		-1,
    		-1,
    		-1,
    		-1,
    		0,
    		1,
    		2
    };
    
    public static final HardwareConnection[] i2cPins = new HardwareConnection[] {
    new HardwareConnection(GroveTwig.I2C,0),
    new HardwareConnection(GroveTwig.I2C,1)
    };
    
    public final static int DATA_RAW_VOLTAGE = 0;
    public final static int CLOCK_RAW_VOLTAGE = 1;
    public final static int HIGH_LINE_VOLTAGE_MARK = 1 << 8; //This is a number needing 9 or more full bits.
}
