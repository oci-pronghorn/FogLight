package com.ociweb.device.impl;

import com.ociweb.device.Connect;

public class EdisonGPIO {

    public static final EdisonPinManager gpioPinMux = new EdisonPinManager(EdisonConstants.GPIO_PIN_MUX);
    public static final EdisonPinManager gpioPinMuxExt = new EdisonPinManager(EdisonConstants.GPIO_PIN_MUX_EXT);
    public static final EdisonPinManager gpioPinModes = new EdisonPinManager(EdisonConstants.GPIO_PIN_MODES);
    public static final EdisonPinManager gpioLinuxPins = new EdisonPinManager(EdisonConstants.GPIO_PINS);
    public static final EdisonPinManager gpioOutputEnablePins = new EdisonPinManager(EdisonConstants.OUTPUT_ENABLE);
    public static final EdisonPinManager gpioPullupEnablePins = new EdisonPinManager(EdisonConstants.PULL_UP_ENABLE);
    public static final EdisonPinManager shieldControl = new EdisonPinManager(EdisonConstants.SHIELD_CONTROL);
    
    public static void ensureAllLinuxDevices(Connect[] usedLines) {
        
        shieldControl.ensureDevice(0); //tri statebyte
        shieldControl.ensureDevice(1); //shield reset
    
        int j = usedLines.length;
        while (--j>=0) {                
            int i = usedLines[j].connection;
            gpioLinuxPins.ensureDevice(i);
            gpioOutputEnablePins.ensureDevice(i);
            gpioPullupEnablePins.ensureDevice(i);   
            gpioPinMux.ensureDevice(i);
            gpioPinMuxExt.ensureDevice(i);
            gpioPinModes.ensureDevice(i);
        }
        
        //Required for pre-setup of analog pins
        gpioOutputEnablePins.ensureDevice(10);
        gpioOutputEnablePins.ensureDevice(11);
        gpioOutputEnablePins.ensureDevice(12);
        gpioOutputEnablePins.ensureDevice(13);
        
    }
    public static void removeAllLinuxDevices(Connect[] usedLines) {
        shieldControl.removeDevice(0); //tri state
        shieldControl.removeDevice(1); //shield reset
    
        //NOTE: this is overkill to create every single device we may possibly need
        //      TODO: use some flats to reduce this set to only the ones we are using
        
        int j = usedLines.length;
        while (--j>=0) {                
            int i = usedLines[j].connection;       
            gpioLinuxPins.removeDevice(i);
            gpioOutputEnablePins.removeDevice(i);
            gpioPullupEnablePins.removeDevice(i); 
            gpioPinMux.removeDevice(i);
            gpioPinMuxExt.removeDevice(i);
            gpioPinModes.removeDevice(i);
        }
        
      //Required for pre-setup of analog pins
      gpioOutputEnablePins.removeDevice(10);
      gpioOutputEnablePins.removeDevice(11);
      gpioOutputEnablePins.removeDevice(12);
      gpioOutputEnablePins.removeDevice(13);
        
    }
    public static void configDigitalInput(int dPort) {       
        gpioOutputEnablePins.setDirectionLow(dPort);
    
        //no need to map since ports happen to match the digital pins
        gpioPullupEnablePins.setDirectionHigh(dPort);
        gpioLinuxPins.setDirectionIn(dPort);      
    }
    public static void configAnalogInput(int aPort) {
        if (aPort<0 || aPort>5) {
            throw new UnsupportedOperationException("only available on 0, 1, 2, or 3 and only 4 or 5 if I2C is not in use.");
        }
                
        gpioPinMux.setDirectionHigh(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);        
        gpioOutputEnablePins.setDirectionLow(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);
        gpioPullupEnablePins.setDirectionIn(EdisonConstants.ANALOG_CONNECTOR_TO_PIN[aPort]);  //in      
        
    }

}
