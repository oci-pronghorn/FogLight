package com.ociweb.iot.hardware;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class Hardware {
    
    private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
    public static final int MAX_MOVING_AVERAGE_SUPPORTED = 101;
    
    private static final HardConnection[] EMPTY = new HardConnection[0];
    
    public boolean configI2C;       //Humidity, LCD need I2C address so..
    public long debugI2CRateLastTime;
    
    public HardConnection[] multiBitInputs; //Rotary Encoder, and similar
    public HardConnection[] multiBitOutputs;//Some output that takes more than 1 bit
    
    public HardConnection[] digitalInputs; //Button, Motion
    public HardConnection[] digitalOutputs;//Relay Buzzer
    
    public HardConnection[] analogInputs;  //Light, UV, Moisture
    public HardConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)
    
    private long timeTriggerRate;
    
    public GraphManager gm;
    
    //TODO: ma per field with max defined here., 
    //TODO: publish with or with out ma??
    
    //only publish when the moving average changes
    
    private ReentrantLock lock = new ReentrantLock();
    
    public Hardware() {
        this(false,false,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY);
    }
    
    protected Hardware(boolean publishTime, boolean configI2C, HardConnection[] multiDigitalInput,
            HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs) {
        
        this.configI2C = configI2C;
        this.multiBitInputs = multiDigitalInput; 
        //TODO: add multiBitOutputs and support for this new array
                
        this.digitalInputs = digitalInputs;
        this.digitalOutputs = digitalOutputs;
        this.pwmOutputs = pwmOutputs;
        this.analogInputs = analogInputs;
    }

    /////
    /////
    
    HardConnection[] growConnections(HardConnection[] original, HardConnection toAdd) {
        int l = original.length;
        HardConnection[] result = new HardConnection[l+1];
        System.arraycopy(original, 0, result, 0, l);
        result[l] = toAdd;
        return result;
    }
    
    public Hardware useConnectA(IODevice t, int connection) {
        return useConnectA(t,connection,-1);
    }
    
    public Hardware useConnectA(IODevice t, int connection, int customRate) {
        HardConnection gc = new HardConnection(t,connection,ConnectionType.GrovePi);
        if (t.isInput()) {
            assert(!t.isOutput());
            analogInputs = growConnections(analogInputs, gc);
        } else {
            assert(t.isOutput());
            pwmOutputs = growConnections(pwmOutputs, gc);
        }
        return this;
    }
    
    public Hardware useConnectD(IODevice t, int connection) {
        return useConnectD(t,connection,-1);
    }
    
    public Hardware useConnectD(IODevice t, int connection, int customRate) {
        HardConnection gc = new HardConnection(t,connection,ConnectionType.GrovePi);
        if (t.isInput()) {
            assert(!t.isOutput());
            digitalInputs = growConnections(digitalInputs, gc);
        } else {
            assert(t.isOutput());
            digitalOutputs = growConnections(digitalOutputs, gc);
        }
        return this;
    }  
    

    public Hardware useConnectDs(IODevice t, int ... connections) {

        if (t.isInput()) {
            assert(!t.isOutput());
            for(int con:connections) {
                multiBitInputs = growConnections(multiBitInputs, new HardConnection(t,con,ConnectionType.GrovePi));
            }
            
          System.out.println("connections "+Arrays.toString(connections));  
          System.out.println("Encoder here "+Arrays.toString(multiBitInputs));  
            
        } else {
            assert(t.isOutput());
            for(int con:connections) {
                multiBitOutputs = growConnections(multiBitOutputs, new HardConnection(t,con,ConnectionType.GrovePi));
            }
        }
        return this;
        
    }  
    
    public Hardware useTriggerRate(long rateInMS) {
        timeTriggerRate = rateInMS;
        return this;
    }
    
    public long getTriggerRate() {
        return timeTriggerRate;
    }
    
    public Hardware useI2C() {
        this.configI2C = true;
        return this;
    }
    
    /////
    /////


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
    
    public abstract int digitalRead(int connector); //Platform specific
    public abstract int analogRead(int connector); //Platform specific
    public abstract void digitalWrite(int connector, int value); //Platform specific
    public abstract void analogWrite(int connector, int value); //Platform specific
    
    
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
    public abstract boolean i2cReadClockBool();
    public abstract boolean i2cReadDataBool();
    
    public abstract void coldSetup();
    public abstract void cleanup();
    public abstract byte getI2CConnector();

    static final boolean debug = false;
    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend) {
        
        if (debug) {
        
            long now = System.nanoTime();
    
            long duration = now-debugI2CRateLastTime;
            if (duration<PureJavaI2CStage.NS_PAUSE) {
                System.err.println("calling I2C too fast");
            }
            if (duration >= 35_000_000) {
                System.err.println("calling I2C too slow "+duration+" devices may have now timed out. next is "+taskAtHand+":"+stepAtHand);
            } else if (duration> 1_000_000 /*20_000_000*/) {
                System.err.println("warning calling I2C too slow "+duration+". next is "+taskAtHand+":"+stepAtHand);
            }
            debugI2CRateLastTime = System.nanoTime();//use new time because we must avoid recording our log message in the duration time.
        }
    }

    public void shutdown() {
        // TODO The caller would like to stop the operating system cold, need platform specific call?
    }


	

	



    
}