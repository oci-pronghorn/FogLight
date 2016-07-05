package com.ociweb.iot.hardware;

import com.ociweb.iot.hardware.impl.edison.EdisonConstants;
import com.ociweb.iot.hardware.impl.edison.EdisonGPIO;
import com.ociweb.iot.hardware.impl.edison.EdisonPinManager;

public class I2CLowLevelEdisonController implements I2CLowLevelContoller {

    
    public void setup() {
        
        beginPinConfiguration();

                EdisonGPIO.gpioPinMux.setDirectionLow(18);
                EdisonGPIO.gpioPinMux.setDirectionLow(19);
                
          //      gpioLinuxPins.setDirectionIn(18); //in
          //      gpioLinuxPins.setDirectionIn(19);  //in
                
           //     gpioOutputEnablePins.setDirectionLow(18); //low
            //    gpioOutputEnablePins.setDirectionLow(19); //low
                
                EdisonGPIO.gpioPullupEnablePins.setDirectionIn(18); 
                EdisonGPIO.gpioPullupEnablePins.setDirectionIn(19);
        //        gpioPullupEnablePins.setDirectionOut(18); 
          //      gpioPullupEnablePins.setDirectionOut(19);
                
                
                EdisonGPIO.gpioPinModes.setDebugCurrentPinmuxMode1(19);
                EdisonGPIO.gpioPinModes.setDebugCurrentPinmuxMode1(18);
                
                EdisonGPIO.gpioLinuxPins.setDirectionOut(19); //Must be set to allow for read/write
                EdisonGPIO.gpioLinuxPins.setDirectionOut(18); //Must be set to allow for read/write
                
                EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(19); //Must be set to allow values to sick//enable us to be master of the bus not just an observer
                EdisonGPIO.gpioOutputEnablePins.setDirectionHigh(18); //Must be set to allow values to sick
                //NOTE: for Arduino breakout board and Edison only i2c-6  is supported 
                
              //  Path i2cDevice = Paths.get("/sys/class/i2c-dev/i2c-6");

        
        endPinConfiguration();
        
        
    }
    
    private void beginPinConfiguration() {
        EdisonGPIO.shieldControl.setDirectionLow(0);
    }
    
    private void endPinConfiguration() {
        EdisonGPIO.shieldControl.setDirectionHigh(0);
    }
    
    public void i2cDataIn() {
        EdisonGPIO.configI2CDataIn();
    }
    
    public void i2cDataOut() {
        EdisonGPIO.configI2CDataOut();
    }
    
    public void i2cClockIn() {
        EdisonGPIO.configI2CClockIn();
    }
    
    public void i2cClockOut() {
        EdisonGPIO.configI2CClockOut();
    }

    public boolean i2cReadAck() {
        return EdisonPinManager.analogRead(EdisonConstants.DATA_RAW_VOLTAGE) < EdisonConstants.HIGH_LINE_VOLTAGE_MARK;

    }
    public boolean i2cReadDataBool() {
        return EdisonPinManager.analogRead(EdisonConstants.DATA_RAW_VOLTAGE) > EdisonConstants.HIGH_LINE_VOLTAGE_MARK;
    }
    
    public boolean i2cReadClockBool() {
        return EdisonPinManager.analogRead(EdisonConstants.CLOCK_RAW_VOLTAGE) > EdisonConstants.HIGH_LINE_VOLTAGE_MARK;
    }
    
    
    public void i2cSetClockLow() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_CLOCK, EdisonPinManager.I2C_LOW, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetClockHigh() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_CLOCK, EdisonPinManager.I2C_HIGH, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetDataLow() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_DATA, EdisonPinManager.I2C_LOW, EdisonGPIO.gpioLinuxPins);
    }

    public void i2cSetDataHigh() {
        EdisonPinManager.writeValue(EdisonPinManager.I2C_DATA, EdisonPinManager.I2C_HIGH, EdisonGPIO.gpioLinuxPins);
    }

    public int i2cReadData() {
        return EdisonPinManager.digitalRead(EdisonPinManager.I2C_DATA);
    }

    public int i2cReadClock() {
        return EdisonPinManager.digitalRead(EdisonPinManager.I2C_CLOCK);
    }

    private long startTime;
    
    @Override
    public void debugI2CRateLastTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend) {
        
        // TODO Not sure what do do here yet.
        
    }


}
