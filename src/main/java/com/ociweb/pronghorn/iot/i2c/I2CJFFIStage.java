package com.ociweb.pronghorn.iot.i2c;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.pronghorn.iot.AbstractTrafficOrderedStage;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;
import com.ociweb.pronghorn.util.Blocker;
import com.ociweb.pronghorn.util.math.PMath;
import com.ociweb.pronghorn.util.math.ScriptedSchedule;

public class I2CJFFIStage extends AbstractTrafficOrderedStage {

	private final I2CBacking i2c;
	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	private final Pipe<I2CResponseSchema> i2cResponsePipe;

	private static final Logger logger = LoggerFactory.getLogger(I2CJFFIStage.class);
	private ScriptedSchedule schedule;

	private I2CConnection[] inputs = null;
	private final boolean hasInputs;
	
	private byte[] workingBuffer;

	private int inProgressIdx = 0;
	private byte[] inErrorCode;
	
	private long blockStartTime = 0;
	private int scheduleIdx = 0;

	private boolean awaitingResponse = false;

	private static final int MAX_ADDR = 127;


	private long timeOut = 0;
	private final int writeTime = 5; //it often takes 1 full ms just to contact the linux driver so this value must be a minimum of 3ms.

	//NOTE: on the pi without any RATE value this stage is run every .057 ms, this is how long 1 run takes to complete for the clock., 2 analog sensors.


	public I2CJFFIStage(GraphManager graphManager, Pipe<TrafficReleaseSchema>[] goPipe, 
			Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			Pipe<TrafficAckSchema>[] ackPipe, 
			Pipe<I2CResponseSchema> i2cResponsePipe,
			Hardware hardware) { 
		super(graphManager, hardware, i2cPayloadPipes, goPipe, ackPipe, i2cResponsePipe); 
		this.i2c = hardware.i2cBacking;
		this.fromCommandChannels = i2cPayloadPipes;
		this.i2cResponsePipe = i2cResponsePipe;
		
		//force all commands to happen upon publish and release
		this.supportsBatchedPublish = false;
		this.supportsBatchedRelease = false;

		
		this.inputs = null==hardware.i2cInputs?new I2CConnection[0]:hardware.i2cInputs;
		this.hasInputs = inputs.length>0;
		if (this.hasInputs) {
    		int[] schedulePeriods = new int[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                schedulePeriods[i] = inputs[i].responseMS;
            }
    		this.schedule = PMath.buildScriptedSchedule(schedulePeriods);
    		
    		GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, (this.schedule.commonClock*1_000_000)/10 , this); 
		}
	}

	@Override
	public void startup(){
		super.startup();

		workingBuffer = new byte[2048];
		
		inErrorCode = new byte[MAX_ADDR];
		Arrays.fill(inErrorCode, (byte)-2);

		logger.info("Polling "+this.inputs.length+" i2cInput(s)");

		for (int i = 0; i < inputs.length; i++) {
			timeOut = hardware.currentTimeMillis() + writeTime;
			while(!i2c.write(inputs[i].address, inputs[i].setup, inputs[i].setup.length) && hardware.currentTimeMillis()<timeOut){};
			logger.info("I2C setup {} complete",inputs[i].address);
		}
		//TODO: add setup for outputs ??

		

		logger.info("proposed schedule: {} ",schedule);

		blockStartTime = hardware.currentTimeMillis();
	}


	@Override
	public void run() {
	
	    //never run poll if we have nothing to poll, in that case the array will have a single -1 
	    if (hasInputs && hasListeners()) {
	        do {
        	    long waitTime = blockStartTime -hardware.currentTimeMillis();
        		if(waitTime>0){
        		   //do commands now while we wait for the next block. 
        		   processReleasedCommands(waitTime);
        		   waitTime = blockStartTime -hardware.currentTimeMillis();
        		   if (waitTime>0) {
        		       return; //Enough time has not elapsed to start next block on schedule
        		   }
        		}
        
        		do{
        			inProgressIdx = schedule.script[scheduleIdx];
        			scheduleIdx = (scheduleIdx+1) % schedule.script.length;
        			
        			if(inProgressIdx != -1) {
        			    
                        I2CConnection connection = this.inputs[inProgressIdx];
                        timeOut = hardware.currentTimeMillis() + writeTime;
      
                        while(!i2c.write((byte)connection.address, connection.readCmd, connection.readCmd.length) && hardware.currentTimeMillis()<timeOut){};
        				;
        				workingBuffer[0] = inErrorCode[inProgressIdx];
        				if (-3 == workingBuffer[0]) {
        				    if (PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
                                PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, this.inputs[inProgressIdx].address);
                                PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, workingBuffer, 0, 1, Integer.MAX_VALUE);
                                PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, hardware.currentTimeMillis());
                                PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, this.inputs[inProgressIdx].register);
                                PipeWriter.publishWrites(i2cResponsePipe);  
                                workingBuffer[0] = inErrorCode[inProgressIdx] = -2;
                            }
        				} 	
        				
        				long now = System.nanoTime();
        				while(System.nanoTime() < now + this.inputs[inProgressIdx].delayAfterRequestNS) {
        				    Thread.yield();
        				    if (Thread.interrupted()) {
        				        requestShutdown();
        				        return;
        				    }
        				}

        				readI2CData(this.inputs[inProgressIdx].readBytes);	
        				
        			}
        
        		}while(inProgressIdx != -1);
        		blockStartTime += schedule.commonClock;
        		
	        } while (true);
	    } else {
	        processReleasedCommands(10);
	    }
	}

	private boolean hasListeners() {
		return i2cResponsePipe != null;
	}

    private void readI2CData(int len) {
          
        if (PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
        	PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, this.inputs[inProgressIdx].address);

        	byte[] temp =i2c.read(this.inputs[inProgressIdx].address, workingBuffer, len);
        	PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, temp, 0, len, Integer.MAX_VALUE);
        	
        	PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, hardware.currentTimeMillis());
        	PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, this.inputs[inProgressIdx].register);
        	PipeWriter.publishWrites(i2cResponsePipe);    					
        }else{
            System.out.println("pipe is full");
            inErrorCode[inProgressIdx]=-3;
        }
    }




	protected void processMessagesForPipe(int a) {
		sendOutgoingCommands(a);

	}

	private void sendOutgoingCommands(int activePipe) {
		
		if(activePipe == -1){
			return; //No active pipe selected yet
		}
		
		Pipe<I2CCommandSchema> pipe = fromCommandChannels[activePipe];

		while ( hasReleaseCountRemaining(activePipe) 
				&& !isChannelBlocked(activePipe)
				&& !connectionBlocker.isBlocked(Pipe.peekInt(pipe, 1)) //peek next connection and check that it is not blocking for some time 
				&& PipeReader.tryReadFragment(pipe)){

			int msgIdx = PipeReader.getMsgIdx(pipe);

			switch(msgIdx){
    			case I2CCommandSchema.MSG_COMMAND_7:
    			{
    			    int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11);
    				int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12);
    
    				byte[] backing = PipeReader.readBytesBackingArray(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
    				int len  = PipeReader.readBytesLength(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
    				int pos = PipeReader.readBytesPosition(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
    				int mask = PipeReader.readBytesMask(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
    
    				assert(!connectionBlocker.isBlocked(connection)): "expected command to not be blocked";
    
    				Pipe.copyBytesFromToRing(backing, pos, mask, workingBuffer, 0, Integer.MAX_VALUE, len);
    
    				try {
    					if (logger.isDebugEnabled()) {
    						logger.debug("{} send command {} {}", activePipe, Appendables.appendArray(new StringBuilder(), '[', backing, pos, mask, ']', len), pipe);
    					}
    				} catch (IOException e) {
    					throw new RuntimeException(e);
    				}
    
    				timeOut = hardware.currentTimeMillis() + writeTime;
    				while(!i2c.write((byte) addr, workingBuffer, len) && hardware.currentTimeMillis()<timeOut){}
    
    				logger.debug("send done");
    
    			}                                      
    			break;
    
    			case I2CCommandSchema.MSG_BLOCKCHANNELMS_22:
    			{
    				blockChannelDuration(activePipe,PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13));            	   
    				logger.debug("CommandChannel blocked for {} millis ",PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13));
    			}
    			break;
    
    			case I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20:
    			{  
    			    int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_CONNECTOR_11);
    				int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_ADDRESS_12);
    				long duration = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_DURATION_13);
    				
    				//TODO: urgent this blocker is picking up the i2c address which is the same for all twigs on the pi.
    				
    				connectionBlocker.until(connection, hardware.currentTimeMillis() + duration);
    				logger.debug("I2C addr {} {} blocked for {} millis  {}", addr, connection, duration, pipe);
    			}   
    			break;
    
    			case I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21:
    			{  
    			    int connection = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_CONNECTOR_11);
    				int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12);
    				long time = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14);
    				connectionBlocker.until(connection, time);
    				logger.debug("I2C addr {} {} blocked until {} millis {}", addr, connection, time, pipe);
    			}
    
    			break;    
    			case -1 :
    				requestShutdown();      

			}
			PipeReader.releaseReadLock(pipe);

			//only do now after we know its not blocked and was completed
			decReleaseCount(activePipe);

		}

	}



}