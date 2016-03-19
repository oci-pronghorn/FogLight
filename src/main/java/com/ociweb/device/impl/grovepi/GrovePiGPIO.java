package com.ociweb.device.impl.grovepi;

import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.impl.grovepi.GrovePiPinManager;

/**
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
            final int i = usedLines[j].connection;
            gpioLinuxPins.ensureDevice(i);
            gpioOutputEnablePins.ensureDevice(i);
            gpioPullupEnablePins.ensureDevice(i);
            gpioPinModes.ensureDevice(i);
        }
    }

    public static void removeAllLinuxDevices(final GroveConnect[] usedLines) {
        int j = usedLines.length;
        while (--j >= 0) {
            final int i = usedLines[j].connection;
            gpioLinuxPins.removeDevice(i);
            gpioOutputEnablePins.removeDevice(i);
            gpioPullupEnablePins.removeDevice(i);
            gpioPinModes.removeDevice(i);
        }
    }

    public static void configDigitalInput(final int dPort) {
        gpioOutputEnablePins.setDirectionLow(dPort);
        gpioPullupEnablePins.setDirectionHigh(dPort);
        gpioLinuxPins.setDirectionIn(dPort);
    }

    public static void configPWM(final int dPort) {
        if ((dPort < 3) || (4 == dPort) || (7 == dPort) || (8 == dPort)
            || (dPort > 11)) // (only 3, 5, 6, 9, 10, 11)
            throw new UnsupportedOperationException("PWM only available on 3, 5, 6, 9, 10 or 11");
        
        gpioOutputEnablePins.setDirectionHigh(dPort);
        gpioPullupEnablePins.setDirectionIn(dPort);
    }

    public static void configDigitalOutput(final int dPort) {
        gpioPullupEnablePins.setDirectionHigh(dPort);
        gpioOutputEnablePins.setDirectionHigh(dPort);
        gpioLinuxPins.setDirectionOut(dPort);
    }
    public static void configI2CClockOut() {
        gpioLinuxPins.setDirectionOut(1);
        gpioOutputEnablePins.setDirectionHigh(1);
    }

    public static void configI2CClockIn() {
        gpioLinuxPins.setDirectionIn(1); // in
        gpioOutputEnablePins.setDirectionLow(1);
    }

    public static void configI2CDataOut() {
        gpioLinuxPins.setDirectionOut(0);
        gpioOutputEnablePins.setDirectionHigh(0);
    }

    public static void configI2CDataIn() {
        gpioLinuxPins.setDirectionIn(0);
        gpioOutputEnablePins.setDirectionLow(0);
    }

    public static void configI2C() {
        gpioPullupEnablePins.setDirectionIn(0);
        gpioPullupEnablePins.setDirectionIn(1);
        
        gpioLinuxPins.setDirectionOut(1);
        gpioLinuxPins.setDirectionOut(0);
        gpioOutputEnablePins.setDirectionHigh(1);
        gpioOutputEnablePins.setDirectionHigh(0);
    }
}
