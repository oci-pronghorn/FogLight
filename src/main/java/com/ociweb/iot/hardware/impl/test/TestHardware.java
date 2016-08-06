package com.ociweb.iot.hardware.impl.test;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.impl.DefaultCommandChannel;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;

public class TestHardware extends Hardware {

    private static final int MAX_PINS = 127;
    
    private final int[] pinData = new int[MAX_PINS];
    
    public boolean isInUnitTest = false;
    
    private final int[] pinHighValues = new int[MAX_PINS];
    private final long[] firstTime = new long[MAX_PINS];
    private final long[] lastTime = new long[MAX_PINS];
    
    private static final Logger logger = LoggerFactory.getLogger(TestHardware.class);
    
    private long lastProvidedTime;
    
    public TestHardware(GraphManager gm) {
        super(gm, new TestI2CBacking());
    }
    
    
    public void setI2CValueToRead(byte address, byte[] data, int length) {
    	TestI2CBacking testBacking = (TestI2CBacking)i2cBacking;
    	testBacking.setValueToRead(address, data, length);
    }
    
    public void clearI2CWriteCount() {
        TestI2CBacking testBacking = (TestI2CBacking)i2cBacking;
        testBacking.clearWriteCount();
    }
    
    public int getI2CWriteCount() {
        TestI2CBacking testBacking = (TestI2CBacking)i2cBacking;
        return testBacking.getWriteCount();
    }
    
    public <A extends Appendable>A outputLastI2CWrite(A target, int back) {
        assert(back>0);
        assert(back<TestI2CBacking.MAX_BACK_MASK);
        TestI2CBacking testBacking = (TestI2CBacking)i2cBacking;
        testBacking.outputLastI2CWrite(target, back);
        return target;
        
    }
    
    
    @Override
    public void coldSetup() {
        clearCaputuredHighs();
        clearCaputuredFirstTimes();
        clearCaputuredLastTimes();
    }
    
    public void clearCaputuredHighs() {
       Arrays.fill(pinHighValues, Integer.MIN_VALUE);
    }
    
    public void clearCaputuredFirstTimes() {
        Arrays.fill(firstTime, 0);
    }
    
    public void clearCaputuredLastTimes() {
        Arrays.fill(lastTime, 0);
    }
    
    public int getCapturedHigh(int connector) {
        return pinHighValues[connector];
    }    
    
    public long getFirstTime(int connector) {
        return firstTime[connector];
    }    
    
    public long getLastTime(int connector) {
        return lastTime[connector];
    }  
    
    @Override
    public int digitalRead(int connector) {
        return pinData[connector];
    }

    @Override
    public int analogRead(int connector) {
        return pinData[connector];
    }

    @Override
    public void digitalWrite(int connector, int value) {
        pinHighValues[connector] = Math.max(pinHighValues[connector], value);
        pinData[connector]=value;
        lastTime[connector] = lastProvidedTime;
        if (0==firstTime[connector]) {
            firstTime[connector]=lastProvidedTime;
        }
        logger.info("digital connection {} set to {} at {}",connector,value,lastProvidedTime);
    }

    @Override
    public void analogWrite(int connector, int value) {
        pinHighValues[connector] = Math.max(pinHighValues[connector], value);
        pinData[connector]=value;
        lastTime[connector] = lastProvidedTime;
        if (0==firstTime[connector]) {
            firstTime[connector]=lastProvidedTime;
        }        
        logger.info("analog connection {} set to {} at {}",connector,value,lastProvidedTime);
    }
    
    @Override
    public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe) {    
       return new DefaultCommandChannel(gm, this, pipe, i2cPayloadPipe, messagePubSub, orderPipe);   //TODO: urgent rename as DefaultCommadnChannel     
    }
    
    @Override
    public StageScheduler createScheduler(DeviceRuntime iotDeviceRuntime) {

        if (isInUnitTest) {
                      
            //NOTE: need to consider different schedulers in the future.
           return new NonThreadScheduler(gm);
        } else {
           return super.createScheduler(iotDeviceRuntime);
        }
       
       
    }
    
    
    @Override
    public long currentTimeMillis() {
        return lastProvidedTime = super.currentTimeMillis();
    }

    public long lastProvidedTimeMillis() {
        return lastProvidedTime;
    }
    
    public ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return new ReactiveListenerStage(gm, listener, inputPipes, outputPipes, this);
    }

    
}
