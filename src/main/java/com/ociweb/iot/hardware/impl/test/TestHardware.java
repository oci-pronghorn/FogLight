package com.ociweb.iot.hardware.impl.test;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.api.MsgRuntime;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.HardwarePlatformType;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.hardware.impl.DefaultCommandChannel;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.ReactiveIoTListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.rs232.RS232Clientable;
import com.ociweb.pronghorn.pipe.Pipe;
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
    
    private RS232Clientable testSerial = new TestSerial();

    public TestPortReader portReader = new SinTestPortReader();
    
    public TestHardware(GraphManager gm, String[] args) {
        super(gm, args, 1);
        //logger.trace("You are running on the test hardware.");

    }
	
	public I2CBacking getI2CBacking() {
		if (null == i2cBackingInternal) {
			i2cBackingInternal = new TestI2CBacking().configure((byte) 1);
		}
		return i2cBackingInternal;		
	}
    
    public void enableTelemetry(boolean enable) {
    	if (!isInUnitTest && enable) {
    		super.enableTelemetry();
    	}
    	//else do nothing this is a test.
    }
    
    
    protected RS232Clientable buildSerialClient() {
     	return testSerial;
    }
    
    public void setI2CValueToRead(byte address, byte[] data, int length) {
    	TestI2CBacking testBacking = (TestI2CBacking)getI2CBacking();
    	testBacking.setValueToRead(address, data, length);
    }
    
    public void clearI2CWriteCount() {
        TestI2CBacking testBacking = (TestI2CBacking)getI2CBacking();
        testBacking.clearWriteCount();
    }
    
    public int getI2CWriteCount() {
        TestI2CBacking testBacking = (TestI2CBacking)getI2CBacking();
        return testBacking.getWriteCount();
    }
    
    public <A extends Appendable>A outputLastI2CWrite(A target, int back) {
        assert(back>0);
        assert(back<TestI2CBacking.MAX_BACK_MASK);
        TestI2CBacking testBacking = (TestI2CBacking)getI2CBacking();
        testBacking.outputLastI2CWrite(target, back);
        return target;
        
    }
    
    public <A extends Appendable> A outputLastSerialWrite(A target, int back){
    	byte[] output = new byte[100];
    	testSerial.readInto(output, 0, 20, output, 0, 0);
    	for (byte b: output){
    		System.out.println(b + ", ");
    	}
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
        IODevice connectedDevice = getConnectedDevice(port);
        int range = connectedDevice == null ? 255 : connectedDevice.range();
        return portReader.read(port, pinData[port.port], range);
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
    public DefaultCommandChannel newCommandChannel(int features, int instance,
    		                                   PipeConfigManager pcm) {    
       return new DefaultCommandChannel(gm, this, features, instance, pcm);       
    }
    
    @Override
    public DefaultCommandChannel newCommandChannel(int instance, 
    		                                     PipeConfigManager pcm) {    
       return new DefaultCommandChannel(gm, this, 0, instance, pcm);     
    }
    
    @Override
    public StageScheduler createScheduler(MsgRuntime runtime) {

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
    
    @Override
    public <R extends ReactiveListenerStage> R createReactiveListener(GraphManager gm,  Behavior listener, 
    		                                                 Pipe<?>[] inputPipes, Pipe<?>[] outputPipes,
    		                                                 ArrayList<ReactiveManagerPipeConsumer> consumers,
    		                                                 int parallelInstance, String nameId) {
        assert(null!=listener);
    	return (R)new ReactiveIoTListenerStage(gm, listener,
        		                               inputPipes, outputPipes, 
        		                               consumers, this, parallelInstance, nameId);
    }
	public final boolean isTestHardware() {
		return true;
	}
	
	public void setSerialEcho(boolean on){
		((TestSerial)testSerial).setEcho(on);//TODO: is this hard cast okay?
	}
    
}
