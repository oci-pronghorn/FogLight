package com.ociweb.device.impl;

public class EdisonGPIO {

    public static final EdisonPinManager gpioPinMux = new EdisonPinManager(EdisonConstants.GPIO_PIN_MUX);
    public static final EdisonPinManager gpioPinMuxExt = new EdisonPinManager(EdisonConstants.GPIO_PIN_MUX_EXT);
    public static final EdisonPinManager gpioPinModes = new EdisonPinManager(EdisonConstants.GPIO_PIN_MODES);
    public static final EdisonPinManager gpioLinuxPins = new EdisonPinManager(EdisonConstants.GPIO_PINS);
    public static final EdisonPinManager gpioOutputEnablePins = new EdisonPinManager(EdisonConstants.OUTPUT_ENABLE);
    public static final EdisonPinManager gpioPullupEnablePins = new EdisonPinManager(EdisonConstants.PULL_UP_ENABLE);
    public static final EdisonPinManager shieldControl = new EdisonPinManager(EdisonConstants.SHIELD_CONTROL);

}
