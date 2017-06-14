package com.ociweb.iot.hardware.impl.test;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.gl.impl.schema.TrafficOrderSchema;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.HardwarePlatformType;
import com.ociweb.iot.hardware.impl.DefaultCommandChannel;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.ReactiveListenerStageIOT;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.network.schema.ClientHTTPRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;

public class TestHardware extends HardwareImpl {

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
        //logger.trace("You are running on the test hardware.");
    }
    
    public void enableTelemetry(boolean enable) {
    	//do nothing this is a test.
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
    
    public int getCapturedHigh(Port port) {
        return pinHighValues[port.port];
    }    
    
    public long getFirstTime(Port  port) {
        return firstTime[port.port];
    }    
    
    public long getLastTime(Port port) {
        return lastTime[port.port];
    }

    @Override
    public HardwarePlatformType getPlatformType() {
        return HardwarePlatformType.TEST;
    }

    @Override
    public int read(Port port) {
        return pinData[port.port] + (port.isAnalog() ? (Math.random()<.1 ? 1 : 0) : 0); //adding noise for analog values
    }


    @Override
    public void write(Port port, int value) {
    	
        pinHighValues[port.port] = Math.max(pinHighValues[port.port], value);
        pinData[port.port]=value;
        lastTime[port.port] = lastProvidedTime;
        if (0==firstTime[port.port]) {
            firstTime[port.port]=lastProvidedTime;
        }
        logger.debug("port {} set to {} at {}",port,value,lastProvidedTime);
    }

    
    @Override
    public FogCommandChannel newCommandChannel(int features, int instance, PipeConfigManager pcm) {    
       return new DefaultCommandChannel(gm, this, features, instance, pcm);   //TODO: urgent rename as DefaultCommadnChannel     
    }
    
    @Override
    public FogCommandChannel newCommandChannel(int instance, PipeConfigManager pcm) {    
       return new DefaultCommandChannel(gm, this, 0, instance, pcm);   //TODO: urgent rename as DefaultCommadnChannel     
    }
    
    @Override
    public StageScheduler createScheduler(GreenRuntime runtime) {

        if (isInUnitTest) {
                      
            //NOTE: need to consider different schedulers in the future.
           return new NonThreadScheduler(gm);
        } else {
           return super.createScheduler(runtime);
        }
              
    }
    
    
    @Override
    public long currentTimeMillis() {
        return lastProvidedTime = super.currentTimeMillis();
    }

    public long lastProvidedTimeMillis() {
        return lastProvidedTime;
    }
    
    public <R extends ReactiveListenerStage> R createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return (R)new ReactiveListenerStageIOT(gm, listener, inputPipes, outputPipes, this);
    }

    
}
