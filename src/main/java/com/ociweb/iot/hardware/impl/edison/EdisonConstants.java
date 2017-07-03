package com.ociweb.iot.hardware.impl.edison;

import com.ociweb.iot.grove.AnalogDigitalGroveTwig;
import com.ociweb.iot.grove.I2CGroveTwig;
import com.ociweb.iot.hardware.HardwareConnection;


public class EdisonConstants {

    public static final short[] SHIELD_CONTROL = new short[]{
    214, //tri state output
    215, //shield reset output
    207  //shield reset input
    };
    public static final short[] GPIO_PINS = new short[] {
            130, //IO 0 D 0
            131, //IO 1 D 1
            128, //IO 2 D 2
            12,  //IO 3 D 3 
            129, //IO 4 D 4
            13,  //IO 5 D 5
            182, //IO 6 D 6
            48,  //IO 7 D 7
            49,  //IO 8 D 8
            183, //IO 9 D 9  
            41,  //IO10 D10
            43,  //IO11 D11
            42,  //IO12 D12
            40,  //IO13 D13
            44, //IO14 D14 A0 200
            45, //IO15 D15 A1 201
            46, //IO16 D16 A2 202
            47, //IO17 D17 A3 203
            14, //IO18 D18 A4 204
            165, //IO19 D19 A5 205
    };
    
    public static final short[] PWM_PINS = new short[] {// (only 3, 5, 6, 9, 10 ??, 11 ??) 
            -1, //IO 0 D 0
            -1, //IO 1 D 1
            -1, //IO 2 D 2
             0,  //IO 3 D 3  (PWM)
            -1, //IO 4 D 4
             1,  //IO 5 D 5  (PWM)
             2, //IO 6 D 6   (PWM)
            -1,  //IO 7 D 7
            -1,  //IO 8 D 8
             3, //IO 9 D 9   (PWM)
             4,  //IO10 D10  (PWM ??)
             5,  //IO11 D11  (PWM ??)
            -1,  //IO12 D12
            -1,  //IO13 D13
            -1, //IO14 D14 A0 200
            -1, //IO15 D15 A1 201
            -1, //IO16 D16 A2 202
            -1, //IO17 D17 A3 203
            -1, //IO18 D18 A4 204
            -1, //IO19 D19 A5 205
    };
    
    
    public static final short[] GPIO_PIN_MODES = new short[] {
            130, //IO 0 D 0
            131, //IO 1 D 1
            128, //IO 2 D 2
            12,  //IO 3 D 3 
            129, //IO 4 D 4
            13,  //IO 5 D 5
            182, //IO 6 D 6
            48,  //IO 7 D 7
            49,  //IO 8 D 8
            183, //IO 9 D 9  
            111,  //IO10 D10  41
            115,  //IO11 D11  43
            114,  //IO12 D12  42
            109,  //IO13 D13  40  
            44, //IO14 D14 A0 200
            45, //IO15 D15 A1 201
            46, //IO16 D16 A2 202
            47, //IO17 D17 A3 203
            27, //IO18 D18 A4 204 14
            28, //IO19 D19 A5 205 165
    };
    public static final short[] GPIO_PIN_MUX = new short[] {
            
            -1, //IO 0 D 0
            -1, //IO 1 D 1
            -1, //IO 2 D 2
            -1, //IO 3 D 3 
            -1, //IO 4 D 4
            -1, //IO 5 D 5
            -1, //IO 6 D 6
            -1, //IO 7 D 7
            -1, //IO 8 D 8
            -1, //IO 9 D 9  
            240, //IO10 D10 low GPIO/I2S high GPIO/SPI_FS
            241, //IO11 D11 low GPIO/I2S high GPIO/SPI_TXD
            242, //IO12 D12 low GPIO/I2S high GPIO/SPI_RXD
            243, //IO13 D13 low GPIO/I2S high GPIO/SPI_CLK      
            200, //IO14 D14 low GPIO high A0
            201, //IO15 D15 low GPIO high A1
            202, //IO16 D16 low GPIO high A2
            203, //IO17 D17 low GPIO high A3
            204, //IO18 D18 low GPIO/I2C_SDA high A4
            205, //IO19 D19 low GPIO/I2C_SCL high A5
    };
    //low for PWM high for use GPIO_PIN_MUX value
    public static final short[] GPIO_PIN_MUX_EXT = new short[] {
            
            -1, //IO 0 D 0
            -1, //IO 1 D 1
            -1, //IO 2 D 2
            -1, //IO 3 D 3 
            -1, //IO 4 D 4
            -1, //IO 5 D 5
            -1, //IO 6 D 6
            -1, //IO 7 D 7
            -1, //IO 8 D 8
            -1, //IO 9 D 9  
            263,//IO10 D10  low for PWM high see GPIO_PIN_MUX 240
            262,//IO11 D11  low for PWM high see GPIO_PIN_MUX 241
            -1, //IO12 D12  
            -1, //IO13 D13            
            -1, //IO14 D14 A0 
            -1, //IO15 D15 A1
            -1, //IO16 D16 A2 
            -1, //IO17 D17 A3
            -1, //IO18 D18 A4 
            -1, //IO19 D19 A5
    };
    public static final byte[] ANALOG_CONNECTOR_TO_PIN = new byte[] {
            14, //A0
            15, //A1
            16, //A2
            17, //A3
            18, //A4
            19  //A5
    };
    public static final short[] OUTPUT_ENABLE = new short[] {
            248, //IO 0 D 0
            249, //IO 1 D 1
            250, //IO 2 D 2
            251, //IO 3 D 3 
            252, //IO 4 D 4
            253, //IO 5 D 5
            254, //IO 6 D 6
            255, //IO 7 D 7
            256, //IO 8 D 8
            257, //IO 9 D 9  
            258, //IO10 D10
            259, //IO11 D11
            260, //IO12 D12
            261, //IO13 D13
            232, //IO14 D14 A0
            233, //IO15 D15 A1
            234, //IO16 D16 A2
            235, //IO17 D17 A3
            236, //IO18 D18 A4
            237, //IO19 D19 A5
    };
    public static final short[] PULL_UP_ENABLE = new short[] {
            216, //IO 0 D 0
            217, //IO 1 D 1
            218, //IO 2 D 2
            219,  //IO 3 D 3 
            220, //IO 4 D 4
            221,  //IO 5 D 5
            222, //IO 6 D 6
            223,  //IO 7 D 7
            224,  //IO 8 D 8
            225, //IO 9 D 9  
            226,  //IO10 D10
            227,  //IO11 D11
            228,  //IO12 D12
            229,  //IO13 D13
            208,  //IO14 D14 A0
            209,  //IO15 D15 A1
            210,  //IO16 D16 A2
            211,  //IO17 D17 A3
            212,  //IO18 D18 A4
            213,  //IO19 D19 A5
    };
    
    //TODO: shouls this take in an I2CGroveTwig?
    public static final HardwareConnection[] i2cPins = new HardwareConnection[] {new HardwareConnection(I2CGroveTwig.I2C,18),new HardwareConnection(I2CGroveTwig.I2C,19)};
    public final static int DATA_RAW_VOLTAGE = 4;
    public final static int CLOCK_RAW_VOLTAGE = 5;
    public final static int HIGH_LINE_VOLTAGE_MARK = 1<<8; //This is a number needing 9 or more full bits.

}
