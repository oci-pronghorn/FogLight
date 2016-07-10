package com.ociweb.iot.hardware;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.EdisonCommandChannel;
import com.ociweb.iot.maker.PiCommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class TestHardware extends Hardware {

    private final int[] pinData = new int[127];
    private boolean testAsEdison = true; //if false the digital connections are all done as i2c
        
    
    public TestHardware(GraphManager gm) {
        super(gm, new TestI2CBacking());
    }

    @Override
    public void coldSetup() {
           
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
        pinData[connector]=value;
    }

    @Override
    public void analogWrite(int connector, int value) {
        pinData[connector]=value;
    }

    private byte commandIndex = -1;
    
    @Override
    public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<TrafficOrderSchema> orderPipe) {
        if (testAsEdison) {
            return new EdisonCommandChannel(gm, pipe, i2cPayloadPipe, orderPipe);
        } else {
            this.commandIndex++;
            return new PiCommandChannel(gm, pipe, i2cPayloadPipe, orderPipe, commandIndex);
        }
        
    }

}
