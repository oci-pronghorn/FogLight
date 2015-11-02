package com.ociweb.device.grove;

import com.ociweb.device.grove.schema.GroveI2CRequestSchema;
import com.ociweb.device.grove.schema.GroveI2CSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2I2CRequestStage extends PronghornStage {

    private final Pipe<GroveI2CRequestSchema> input;
    private final Pipe<GroveI2CSchema> output;
    
    protected GroveShieldV2I2CRequestStage(GraphManager graphManager, Pipe<GroveI2CRequestSchema> input, Pipe<GroveI2CSchema> output) {
        super(graphManager, input, output);
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        
        
        //convert request messages into the 
        //right byte stream to be sent to the i2c bus.
        
        // TODO Auto-generated method stub
        
        
    }

}
