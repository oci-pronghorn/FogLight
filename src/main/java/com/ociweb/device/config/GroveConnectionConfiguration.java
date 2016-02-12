package com.ociweb.device.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.ociweb.device.grove.GroveConnect;
import com.ociweb.device.grove.GroveShieldV2I2CStage;
import com.ociweb.device.impl.EdisonGPIO;
import com.ociweb.device.impl.EdisonPinManager;
import com.ociweb.device.testApps.GroveShieldTestApp;

public abstract class GroveConnectionConfiguration {
    
    private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
    public static final int MAX_MOVING_AVERAGE_SUPPORTED = 101;
    
    public final boolean configI2C;       //Humidity, LCD need I2C address so..
    public final GroveConnect[] encoderInputs; //Rotary Encoder
    public final GroveConnect[] digitalInputs; //Button, Motion
    public final GroveConnect[] analogInputs;  //Light, UV, Moisture
    public final GroveConnect[] digitalOutputs;//Relay Buzzer
    public final GroveConnect[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)
    public final boolean publishTime;
    public long lastTime;
    //TODO: ma per field with max defined here., 
    //TODO: publish with or with out ma??
    
    //only publish when the moving average changes
    
    private ReentrantLock lock = new ReentrantLock();
    
    public GroveConnectionConfiguration(boolean publishTime, boolean configI2C, GroveConnect[] encoderInputs,
            GroveConnect[] digitalInputs, GroveConnect[] digitalOutputs, GroveConnect[] pwmOutputs, GroveConnect[] analogInputs) {
        
        this.publishTime = publishTime;
        this.configI2C = configI2C;
        this.encoderInputs = encoderInputs;
        this.digitalInputs = digitalInputs;
        this.digitalOutputs = digitalOutputs;
        this.pwmOutputs = pwmOutputs;
        this.analogInputs = analogInputs;
    }

    public void setToKnownStateFromColdStart() {
        //Not always needed
    }

    public void beginPinConfiguration() {
        try {
            if (!lock.tryLock(PIN_SETUP_TIMEOUT, TimeUnit.SECONDS)) {
                throw new RuntimeException("One of the stages was not able to complete startup due to pin configuration issues.");
            }
        } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
        }
    }
    
    public void endPinConfiguration() {
       lock.unlock();
    }
    
    public abstract int readBit(int connector); //Platform specific
    public abstract int readInt(int connector); //Platform specific

    public abstract void configurePinsForDigitalInput(byte connection); //Platform specific
    public abstract void configurePinsForAnalogInput(byte connection); //Platform specific
    public abstract void configurePinsForI2C();
    
    public int maxAnalogMovingAverage() {
        return MAX_MOVING_AVERAGE_SUPPORTED;
    }

    public abstract void i2cSetClockLow();
    public abstract void i2cSetClockHigh();
    public abstract void i2cSetDataLow();
    public abstract void i2cSetDataHigh();
    public abstract int i2cReadData();
    public abstract int i2cReadClock();
    
    public abstract void i2cDataIn();
    public abstract void i2cDataOut();
    public abstract void i2cClockIn();
    public abstract void i2cClockOut();
    public abstract boolean i2cReadAck();
    
    public abstract void coldSetup();
    public abstract void cleanup();

    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend) {
        
        long now = System.nanoTime();

        long duration = now-lastTime;
        if (duration<GroveShieldV2I2CStage.NS_PAUSE) {
            System.err.println("calling I2C too fast");
        }
        if (duration >= 35_000_000) {
            System.err.println("calling I2C too slow "+duration+" devices may have now timed out. next is "+taskAtHand+":"+stepAtHand);
        } else if (duration> 10_000_000 /*20_000_000*/) {
            System.err.println("warning calling I2C too slow "+duration+". next is "+taskAtHand+":"+stepAtHand);
        }
        lastTime = now;
    }






    
}