package com.ociweb.iot.hardware;

import java.util.concurrent.ThreadLocalRandom;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CBusSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class GroveShieldV2MockImpl extends Hardware {
    
    private final static int HIGH = 1;
    private final static int LOW  = 0;
    
    private final static int IN  = 1;
    private final static int OUT = 0;

    private Pipe<I2CBusSchema> pipe;    
    
    private int clockValue = HIGH; //must be pulled down to low
    private int dataValue = HIGH; //must be pulled down to low
    
    private int dataMode = IN;
    private int clockMode = IN;
        
    
    ThreadLocalRandom r = ThreadLocalRandom.current();
    
    public GroveShieldV2MockImpl(GraphManager gm) {
        super(gm);
    }    
    
    public void addOptionalI2CBusSimulationPipe(Pipe<I2CBusSchema> pipe) {
        this.pipe = pipe;
    }
    
    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend) {
        if (null!=pipe) {
            
            while (! Pipe.isInit(pipe)) {
                //this pipe is used inside the startup of another stage TODO: Need a more elegant way to solve this corner case.
                Thread.yield();
            }
            
            if (PipeWriter.tryWriteFragment(pipe,I2CBusSchema.MSG_STATE_200)) {
                
                PipeWriter.writeInt(pipe, I2CBusSchema.MSG_STATE_200_FIELD_TASK_201, taskAtHand);
                PipeWriter.writeInt(pipe, I2CBusSchema.MSG_STATE_200_FIELD_STEP_202, stepAtHand);
                PipeWriter.writeInt(pipe, I2CBusSchema.MSG_STATE_200_FIELD_BYTE_202, byteToSend);                
                PipeWriter.writeLong(pipe, I2CBusSchema.MSG_STATE_200_FIELD_TIME_103, System.nanoTime());
                PipeWriter.publishWrites(pipe);
                
            } else {
                System.err.println("error pipe is not keeping up with feed ***** ");
                System.err.println("");
                System.exit(0);
            }
        }
        
    }

    private void sendDataPoint() {
        
        if (null!=pipe) {
            
            while (! Pipe.isInit(pipe)) {
                //this pipe is used inside the startup of another stage TODO: Need a more elegant way to solve this corner case.
                Thread.yield();
            }
            
            if (PipeWriter.tryWriteFragment(pipe,I2CBusSchema.MSG_POINT_100)) {
                
                PipeWriter.writeInt(pipe, I2CBusSchema.MSG_POINT_100_FIELD_CLOCK_101, clockValue);
                PipeWriter.writeInt(pipe, I2CBusSchema.MSG_POINT_100_FIELD_DATA_102, dataValue);
                PipeWriter.writeLong(pipe, I2CBusSchema.MSG_POINT_100_FIELD_TIME_103, System.nanoTime());
                PipeWriter.publishWrites(pipe);
                
            } else {
                System.err.println("error pipe is not keeping up with feed");
            }
        }
    }
    
     
    
    @Override
    public int digitalRead(int connector) {
       return r.nextInt(100) > 80 ? 1 : 0;
    }

    @Override
    public int analogRead(int connector) {
        return Math.abs(r.nextInt());
    }
    
    @Override
    public void analogWrite(int connector, int value) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void coldSetup() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cleanup() {
        // TODO Auto-generated method stub

    }

	@Override
	public void digitalWrite(int connector, int value) {
		//TODO:System.out.println();
		//TODO:Log show the log to show the mock process 
		
	}

	@Override
	public byte getI2CConnector() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe,
			Pipe<TrafficOrderSchema> orderPipe) {
		// TODO Auto-generated method stub
		return null;
	}





}
