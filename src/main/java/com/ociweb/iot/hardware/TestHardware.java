package com.ociweb.iot.hardware;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.EdisonCommandChannel;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.PiCommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;

public class TestHardware extends Hardware {

    private final int[] pinData = new int[127];
    private boolean testAsEdison = true; //if false the digital connections are all done as i2c
    
    public static boolean isInUnitTest = false;
    
    private final int[] pinHighValues = new int[127];
    
    private static final Logger logger = LoggerFactory.getLogger(TestHardware.class);
    
    
    public TestHardware(GraphManager gm) {
        super(gm, new TestI2CBacking());
    }

    @Override
    public void coldSetup() {
           
    }
    
    public void clearCaputuredHighs() {
       Arrays.fill(pinHighValues, Integer.MIN_VALUE);
    }
    
    public int getCapturedHigh(int connector) {
        return pinHighValues[connector];
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
        logger.info("digital connection {} set to {}",connector,value);
        pinHighValues[connector] = Math.max(pinHighValues[connector], value);
        pinData[connector]=value;
    }

    @Override
    public void analogWrite(int connector, int value) {
        logger.info("analog connection {} set to {}",connector,value);
        pinHighValues[connector] = Math.max(pinHighValues[connector], value);
        pinData[connector]=value;
    }

    private byte commandIndex = -1;
    
    @Override
    public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe) {
        if (testAsEdison) {
            return new EdisonCommandChannel(gm, pipe, i2cPayloadPipe, messagePubSub, orderPipe);
        } else {
            this.commandIndex++;
            return new PiCommandChannel(gm, pipe, i2cPayloadPipe, messagePubSub, orderPipe, commandIndex);
        }
        
    }
    
    @Override
    public StageScheduler createScheduler(IOTDeviceRuntime iotDeviceRuntime) {

        if (isInUnitTest) {
            //NOTE: need to consider different schedulers in the future.
           return new NonThreadScheduler(gm);
        } else {
           return super.createScheduler(iotDeviceRuntime);
        }
       
       
    }

}
