package com.ociweb.device.impl;

import com.ociweb.device.grove.GroveConnect;

/**
 * TODO: Almost exactly identical to {@link EdisonGPIO}; primary change is
 *       from using pins 19 and 18 for SDA and SCL respectively to using
 *       pins 5 and 3.
 * 
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class GrovePiGPIO {

    public static final GrovePiPinManager gpioPinModes = new GrovePiPinManager(GrovePiConstants.GPIO_PIN_MODES);
    public static final GrovePiPinManager gpioLinuxPins = new GrovePiPinManager(GrovePiConstants.GPIO_PINS);
    public static final GrovePiPinManager gpioOutputEnablePins = new GrovePiPinManager(GrovePiConstants.OUTPUT_ENABLE);
    public static final GrovePiPinManager gpioPullupEnablePins = new GrovePiPinManager(GrovePiConstants.PULL_UP_ENABLE);
    
    public static void ensureAllLinuxDevices(final GroveConnect[] usedLines) {

        int j = usedLines.length;
        while (--j >= 0) {
            System.out.println(usedLines[j].connection);
            System.out.println(usedLines[j].twig.name());
            final int i = usedLines[j].connection;
            gpioLinuxPins.ensureDevice(i);
            gpioOutputEnablePins.ensureDevice(i);
            gpioPullupEnablePins.ensureDevice(i);
            gpioPinModes.ensureDevice(i);
        }
        
        // Required for pre-setup of analog pins
//        gpioOutputEnablePins.ensureDevice(10);
//        gpioOutputEnablePins.ensureDevice(11);
//        gpioOutputEnablePins.ensureDevice(12);
//        gpioOutputEnablePins.ensureDevice(13);
    }

    public static void removeAllLinuxDevices(final GroveConnect[] usedLines) {
        
        // NOTE: this is overkill to create every single device we may possibly
        // need
        // TODO: use some flats to reduce this set to only the ones we are using
        int j = usedLines.length;
        while (--j >= 0) {
            final int i = usedLines[j].connection;
            gpioLinuxPins.removeDevice(i);
            gpioOutputEnablePins.removeDevice(i);
            gpioPullupEnablePins.removeDevice(i);
            gpioPinModes.removeDevice(i);
        }
        
        // Required for pre-setup of analog pins
//        gpioOutputEnablePins.removeDevice(10);
//        gpioOutputEnablePins.removeDevice(11);
//        gpioOutputEnablePins.removeDevice(12);
//        gpioOutputEnablePins.removeDevice(13);
    }

    public static void configDigitalInput(final int dPort) {
        gpioOutputEnablePins.setDirectionLow(dPort);
        
        // no need to map since ports happen to match the digital pins
        gpioPullupEnablePins.setDirectionHigh(dPort);
        gpioLinuxPins.setDirectionIn(dPort);
    }

    public static void configPWM(final int dPort) {
        if ((dPort < 3) || (4 == dPort) || (7 == dPort) || (8 == dPort)
            || (dPort > 11)) // (only 3, 5, 6, 9, 10, 11)
            throw new UnsupportedOperationException("PWM only available on 3, 5, 6, 9, 10 or 11");
        
        gpioOutputEnablePins.setDirectionHigh(dPort);
        gpioPullupEnablePins.setDirectionIn(dPort);
//        gpioPinModes.setDebugCurrentPinmuxMode1(dPort);
    }

    public static void configDigitalOutput(final int dPort) {
//        gpioPinModes.setDebugCurrentPinmuxMode0(dPort);
        
        // no need to map since ports happen to match the digital pins
        gpioPullupEnablePins.setDirectionHigh(dPort);
        gpioOutputEnablePins.setDirectionHigh(dPort);
        gpioLinuxPins.setDirectionOut(dPort);
    }

    // is only supported at 13, Note this disables D10 - D13
    // Grove does not have any sensors using this at the moment
    public static void configSPI() {
//        gpioOutputEnablePins.setDirectionHigh(10);
//        gpioOutputEnablePins.setDirectionHigh(11);
//        gpioOutputEnablePins.setDirectionLow(12);
//        gpioOutputEnablePins.setDirectionHigh(13);
//        
//        gpioPullupEnablePins.setDirectionIn(10);
//        gpioPullupEnablePins.setDirectionIn(11);
//        gpioPullupEnablePins.setDirectionIn(12);
//        gpioPullupEnablePins.setDirectionIn(13);
        
//        gpioPinModes.setDebugCurrentPinmuxMode1(10);
//        gpioPinModes.setDebugCurrentPinmuxMode1(11);
//        gpioPinModes.setDebugCurrentPinmuxMode1(12);
//        gpioPinModes.setDebugCurrentPinmuxMode1(13);
    }

    public static void configI2CClockOut() {
        
        // need to read the ack from the data line sent by the slave
//        gpioLinuxPins.setDirectionOut(5); // Must be set to allow for
                                           // read/write
        gpioLinuxPins.setDirectionOut(1);
        
//        gpioOutputEnablePins.setDirectionHigh(5); // Must be set to allow
                                                   // values to sick
        gpioOutputEnablePins.setDirectionHigh(1);
        
        // gpioPullupEnablePins.setDirectionIn(19);
        // gpioPinMux.setDirectionLow(19);
        // gpioPinModes.setDebugCurrentPinmuxMode1(19);
    }

    //TODO: 5 is SDA1 - I2C on a Pi B+.
    public static void configI2CClockIn() {
        // need to read the ack from the data line sent by the slave
//        gpioLinuxPins.setDirectionIn(5); // in
//        gpioOutputEnablePins.setDirectionLow(5);
        gpioLinuxPins.setDirectionIn(1); // in
        gpioOutputEnablePins.setDirectionLow(1);
        
        // gpioPullupEnablePins.setDirectionIn(19);
        // gpioPinMux.setDirectionLow(19);
        // gpioPinModes.setDebugCurrentPinmuxMode1(19);
    }

    //TODO: 3 is SCL1 - I2C on a Pi B+.
    public static void configI2CDataOut() {
        
        // need to read the ack from the data line sent by the slave
//        gpioLinuxPins.setDirectionOut(3); // Must be set to allow for
//                                           // read/write
//        gpioOutputEnablePins.setDirectionHigh(3); // Must be set to allow
//                                                   // values to sick
        gpioLinuxPins.setDirectionOut(0); // Must be set to allow for
        // read/write
        gpioOutputEnablePins.setDirectionHigh(0); // Must be set to allow
                // values to sick
        
        // gpioPullupEnablePins.setDirectionIn(18);
        // gpioPinMux.setDirectionLow(18);
        // gpioPinModes.setDebugCurrentPinmuxMode1(18);
    }

    public static void configI2CDataIn() {
        
        // need to read the ack from the data line sent by the slave
//        gpioLinuxPins.setDirectionIn(3); // in
//        gpioOutputEnablePins.setDirectionLow(3); // low
        
        gpioLinuxPins.setDirectionIn(0); // in
        gpioOutputEnablePins.setDirectionLow(0); // low
        
        // gpioPullupEnablePins.setDirectionIn(18);
        // gpioPinMux.setDirectionLow(18);
        // gpioPinModes.setDebugCurrentPinmuxMode1(18);
    }

    // is only supported at 18/19, Note this disables use of A4 and A5
    public static void configI2C() {
        // gpioLinuxPins.setDirectionIn(18); //in
        // gpioLinuxPins.setDirectionIn(19); //in
        // gpioOutputEnablePins.setDirectionLow(18); //low
        // gpioOutputEnablePins.setDirectionLow(19); //low
//        gpioPullupEnablePins.setDirectionIn(3);
//        gpioPullupEnablePins.setDirectionIn(5);
        
        gpioPullupEnablePins.setDirectionIn(0);
        gpioPullupEnablePins.setDirectionIn(1);
        
        // gpioPullupEnablePins.setDirectionOut(18);
        // gpioPullupEnablePins.setDirectionOut(19);
//        gpioPinModes.setDebugCurrentPinmuxMode1(5);
//        gpioPinModes.setDebugCurrentPinmuxMode1(3);
        
//        gpioLinuxPins.setDirectionOut(5); // Must be set to allow for
                                           // read/write
        gpioLinuxPins.setDirectionOut(1);
//        gpioLinuxPins.setDirectionOut(3); // Must be set to allow for
                                           // read/write
        gpioLinuxPins.setDirectionOut(0);
        
//        gpioOutputEnablePins.setDirectionHigh(5); // Must be set to allow
                                                   // values to sick//enable us
                                                   // to be master of the bus
                                                   // not just an observer
        gpioOutputEnablePins.setDirectionHigh(1);
//        gpioOutputEnablePins.setDirectionHigh(3); // Must be set to allow
                                                   // values to sick
        gpioOutputEnablePins.setDirectionHigh(0);
        
        // TODO: i2c-1 is only one visible on a GrovePi running the Dexter Debian Wheezy build.
        // NOTE: for Arduino breakout board and GrovePi only i2c-6 is supported
        // Path i2cDevice = Paths.get("/sys/class/i2c-dev/i2c-6");
        System.out.println("I2C enabled for out");
    }

    /**
     * Warning every time this is called both clock and data lines will be set
     * to zero
     */
    private static void configI2COut() {
        // shieldControl.setDirectionLow(0); //3 data 5 clock
//        gpioLinuxPins.setDirectionOut(3); // Must be set to allow for
                                           // read/write
        gpioLinuxPins.setDirectionOut(0);
        
//        gpioOutputEnablePins.setDirectionHigh(3); // Must be set to allow
                                                   // values to sick
            
        gpioOutputEnablePins.setDirectionHigh(0);
        
//        gpioLinuxPins.setDirectionOut(5); // Must be set to allow for
                                           // read/write
        
        gpioLinuxPins.setDirectionOut(1);
        
//        gpioOutputEnablePins.setDirectionHigh(5); // Must be set to allow
                                                   // values to sick
        gpioOutputEnablePins.setDirectionHigh(1);
    }

    public static void configUART(final int dPort) {
        if ((dPort < 0) || (3 == dPort) || (dPort > 4)) // only 0, 1, 2 and 4
            throw new UnsupportedOperationException("UART only available on 0, 1, 2 or 4");
        
        // TODO: UART, there are very few Grove sensors using this. To be
        // implemented later
        throw new UnsupportedOperationException();
    }
}
