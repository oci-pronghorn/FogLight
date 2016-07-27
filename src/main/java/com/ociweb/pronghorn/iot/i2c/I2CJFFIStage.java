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
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;
import com.ociweb.pronghorn.util.Blocker;

public class I2CJFFIStage extends AbstractTrafficOrderedStage {

	private final I2CBacking i2c;
	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	private final Pipe<I2CResponseSchema> i2cResponsePipe;

	private static final Logger logger = LoggerFactory.getLogger(I2CJFFIStage.class);

	private I2CConnection[] inputs = null;
	private byte[] workingBuffer;
	   
    private int inProgressIdx = -1;
    private boolean awaitingResponse = false;
    
    private static final int MAX_ADDR = 127;
    private Blocker pollBlocker;
    
    private long timeOut = 0;
    private final int writeTime = 5; //TODO: Writes time out after 5ms. Is this ideal?
    
	public I2CJFFIStage(GraphManager graphManager, Pipe<TrafficReleaseSchema>[] goPipe, 
			Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			Pipe<TrafficAckSchema>[] ackPipe, 
			Pipe<I2CResponseSchema> i2cResponsePipe,
			Hardware hardware) { 
		super(graphManager, hardware, i2cPayloadPipes, goPipe, ackPipe, i2cResponsePipe); 
		this.i2c = hardware.i2cBacking;
		this.fromCommandChannels = i2cPayloadPipes;
		this.i2cResponsePipe = i2cResponsePipe;

		this.inputs = null==hardware.i2cInputs?new I2CConnection[0]:hardware.i2cInputs;

        //force all commands to happen upon publish and release
        this.supportsBatchedPublish = false;
        this.supportsBatchedRelease = false;
	}

	@Override
	public void startup(){
		super.startup();
		
		workingBuffer = new byte[2048];
		
		logger.debug("Polling "+this.inputs.length+" i2cInput(s)");
		
		for (int i = 0; i < inputs.length; i++) {
			timeOut = hardware.currentTimeMillis() + writeTime;
			while(!i2c.write(inputs[i].address, inputs[i].setup, inputs[i].setup.length) && hardware.currentTimeMillis()<timeOut){};
			 //TODO: add setup for outputs
			logger.info("I2C setup {} complete",inputs[i].address);
		}
		
		pollBlocker = new Blocker(MAX_ADDR);

	}
	
	
	@Override
	public void run() {
	    boolean doneWithActiveCommands = false;
	    //All poll cycles come first as the highest priority. commands will come second and they can back up on the pipe if needed.
	    
	    do {
    	    if (isPollInProgress()) {
    
    	        if (!PipeWriter.hasRoomForFragmentOfSize(i2cResponsePipe, Pipe.sizeOf(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10))) {
    	            return;//no room for response so do not read it now.
    	        }
    	        
    	        int len = this.inputs[inProgressIdx].readBytes;
    	        workingBuffer[0] = -2;
    	        byte[] temp =i2c.read(this.inputs[inProgressIdx].address, workingBuffer, len); 
    	        if (-1 == temp[0]) {
    	            //no data yet with all -1 values
    	            return;
    	        } else {
    	            
    	            if (-2 == temp[0]) {
    	                
    	                //no response and none will follow
    	                //eg the poll failed on this round   
    	                
    	            } else {
    	            
        	            if (!PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
        	                throw new RuntimeException("should not happen "+i2cResponsePipe);
        	            }
        	           
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, this.inputs[inProgressIdx].address);
                        PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, temp, 0, len, Integer.MAX_VALUE);
                        PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, hardware.currentTimeMillis());
                        PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, this.inputs[inProgressIdx].register);
                        PipeWriter.publishWrites(i2cResponsePipe);
                        
    	            }
                                    
                    inProgressIdx--;//read next one
    	        }
    	    }
    	    
    	    long durationToNextRelease = pollBlocker.durationToNextRelease(hardware.currentTimeMillis(), 1000);
    	        	    
    	    //we are not waiting for a response so now its time send the commands.
    	    //will return false if this timed out and we still had work to complete
            doneWithActiveCommands = processReleasedCommands(durationToNextRelease); //no longer than 1 second     
    	    
    	    //check if we should begin polling again
    	    if (isTimeToPoll()) {
    	        sendReadRequest();
    	    }
    	    
	    } while (!doneWithActiveCommands || 
	             isPollInProgress() || 
	             connectionBlocker.willReleaseInWindow(hardware.currentTimeMillis(),msNearWindow) || 
	             pollBlocker.willReleaseInWindow(hardware.currentTimeMillis(), msNearWindow));

	}
	
	private boolean isPollInProgress() {
	    return awaitingResponse && inProgressIdx >= 0;
	}
	
	private boolean isTimeToPoll() {
	       
	    if (inProgressIdx >= 0) {
	        return true; //still processing each of the input items needed
	    }

	    if (this.inputs.length > 0) {
    	    inProgressIdx = this.inputs.length-1; 
    	    return true;
	    } else {
	        return false;
	    }
	}
	
	private void sendReadRequest() {
	    long now = System.currentTimeMillis();
	    pollBlocker.releaseBlocks(now);
	    I2CConnection connection = this.inputs[inProgressIdx];
	    
	    do {
    	    if (!pollBlocker.isBlocked(deviceKey(connection))) {
    	    	timeOut = hardware.currentTimeMillis() + writeTime;
    	        while(!i2c.write((byte)connection.address, connection.readCmd, connection.readCmd.length) && hardware.currentTimeMillis()<timeOut){};
    	        try {
					Thread.sleep(0,1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	        awaitingResponse = true;
    	        //NOTE: the register may or may not be present and the address may not be enough to go on so we MUST 
    	        pollBlocker.until(deviceKey(connection), now+connection.twig.response());
    	        Thread.yield();//provide the system and opportunity to switch.
    	        return;
    	    }
    	    //if that one was blocked check the next.
	    } while (--inProgressIdx >= 0);
        
	}


	private int deviceKey(I2CConnection connection) {
        return  (((int)connection.address)<< 16) | connection.register;
    }

    protected void processMessagesForPipe(int a) {
	    
	    sendOutgoingCommands(a);

	}
		
    private void sendOutgoingCommands(int activePipe) {
        
        Pipe<I2CCommandSchema> pipe = fromCommandChannels[activePipe];
        int pipeKey = -1 -activePipe;
        
        if (connectionBlocker.isBlocked(pipeKey)) {
            return; //TODO: is this blocking right?? TOOD: should this be across stages??
        }
        
        
        while ( hasReleaseCountRemaining(activePipe) 
        		&& !connectionBlocker.isBlocked(Pipe.peekInt(pipe, 1)) //peek next address and check that it is not blocking for some time 
        		&& PipeReader.tryReadFragment(pipe)){

        	int msgIdx = PipeReader.getMsgIdx(pipe);

        	switch(msgIdx){
            	case I2CCommandSchema.MSG_COMMAND_7:
            	{
            		int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12);
    
            		byte[] backing = PipeReader.readBytesBackingArray(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int len  = PipeReader.readBytesLength(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int pos = PipeReader.readBytesPosition(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int mask = PipeReader.readBytesMask(pipe, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);

            		assert(!connectionBlocker.isBlocked(addr)): "expected command to not be blocked";
    
              		Pipe.copyBytesFromToRing(backing, pos, mask, workingBuffer, 0, Integer.MAX_VALUE, len);
              		
              		try {
              		    if (logger.isDebugEnabled()) {
              		        logger.debug("{} send command {} {}", activePipe, Appendables.appendArray(new StringBuilder(), '[', backing, pos, mask, ']', len), pipe);
              		    }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
              		
              		timeOut = hardware.currentTimeMillis() + writeTime;
            		while(!i2c.write((byte) addr, workingBuffer, len) && hardware.currentTimeMillis()<timeOut){};
            		
            		logger.debug("send done");
            		
            	}                                      
            	break;
    
            	case I2CCommandSchema.MSG_BLOCKCHANNELMS_22:
            	{
            	   long duration = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13);
            	   connectionBlocker.until(pipeKey, hardware.currentTimeMillis() + duration);
            	   logger.debug("CommandChannel blocked for {} millis ",duration);
            	}
            	break;
            	
            	case I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20:
            	{  
            		int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_ADDRESS_12);
            		long duration = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_DURATION_13);
            		connectionBlocker.until(addr, hardware.currentTimeMillis() + duration);
            		logger.debug("I2C addr {} blocked for {} millis  {}", addr, duration, pipe);
            	}   
            	break;
            	
                case I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21:
                {  
                    int addr = PipeReader.readInt(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12);
                    long time = PipeReader.readLong(pipe, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14);
                    connectionBlocker.until(addr, time);
                    logger.debug("I2C addr {} blocked until {} millis {}", addr, time, pipe);
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