package com.ociweb.device.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.ociweb.device.grove.GroveConnect;

public abstract class GroveConnectionConfiguration {
    
    private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
    
    public final boolean configI2C;       //Humidity, LCD need I2C address so..
    public final GroveConnect[] encoderInputs; //Rotary Encoder
    public final GroveConnect[] digitalInputs; //Button, Motion
    public final GroveConnect[] analogInputs;  //Light, UV, Moisture
    public final GroveConnect[] digitalOutputs;//Relay Buzzer
    public final GroveConnect[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)
    public final boolean publishTime;
    
    //only publish when the moving average changes
    public final int analogMovingAverage = 21;
    
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

    public int analogMovingAverage() {
        return analogMovingAverage;
    }


    
}