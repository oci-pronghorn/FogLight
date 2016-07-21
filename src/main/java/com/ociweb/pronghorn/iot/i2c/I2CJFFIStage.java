package com.ociweb.pronghorn.iot.i2c;

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
    private long time = 0;
    
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
	}

	@Override
	public void startup(){
		super.startup();
		
		workingBuffer = new byte[2048];
		
		logger.debug("Polling "+this.inputs.length+" i2cInput(s)");
		
		//I2C processing can be very time critical so this thread needs to be on of the highest in priority.
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		for (int i = 0; i < inputs.length; i++) {
			timeOut = System.currentTimeMillis() + writeTime;
			while(!i2c.write(inputs[i].address, inputs[i].setup, inputs[i].setup.length) && System.currentTimeMillis()<timeOut){};
			 //TODO: add setup for outputs
			System.out.println("Setup I2C Device on "+inputs[i].address+" Sent "+Arrays.toString(inputs[i].setup));
			logger.info("I2C setup {} complete",inputs[i].address);
		}
		
		pollBlocker = new Blocker(MAX_ADDR);

	}
	
	
	@Override
	public void run() {

	    if (isPollInProgress()) {

	        if (!PipeWriter.hasRoomForFragmentOfSize(i2cResponsePipe, Pipe.sizeOf(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10))) {
	            return;//no room for response so do not read it now.
	        }
	        
	        int len = this.inputs[inProgressIdx].readBytes;
	        workingBuffer[0]=-2;
	        byte[] temp =i2c.read(this.inputs[inProgressIdx].address, workingBuffer, len); //TODO: this may be forcing a GC between blinks??
	        if (-1 == temp[0]) {
	            //no data yet with all -1 values  s
	            return;
	        } else {
	            
	            if (-2 == temp[0]) {
	                
	                //no response and none will follow
	                //eg the poll failed on this round   
	                
	            } else {
	            
    	            if (!PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
    	                throw new RuntimeException("should not happen "+i2cResponsePipe);
    	            }
    	            
                    logger.debug("Sending reading to Pipe");
                    PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, this.inputs[inProgressIdx].address);
                    PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, temp, 0, len, Integer.MAX_VALUE);
                    PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, System.currentTimeMillis());
                    PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, this.inputs[inProgressIdx].register);
                    PipeWriter.publishWrites(i2cResponsePipe);
                    
	            }
                                
                inProgressIdx--;//read next one
	        }
	    }
	    
	    //we are not waiting for a response so now its time send the commands.
	    super.run(); 
	    
	    //check if we should begin polling again
	    if (isTimeToPoll()) {
	        sendReadRequest();
	    }
	    
	    //if soon keep polling.
	    //pollBlocker.willReleaseInWindow(currentTimeMillis, msNearWindow)
	    
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
	    if (!pollBlocker.isBlocked(connection.address)) {
	    	timeOut = System.currentTimeMillis() + writeTime;
	        while(!i2c.write((byte)connection.address, connection.readCmd, connection.readCmd.length) && System.currentTimeMillis()<timeOut){};
	        awaitingResponse = true;
	        pollBlocker.until(connection.address, now+connection.twig.response());
	    } else {
	        inProgressIdx--;
	    }
        
	}


	protected void processMessagesForPipe(int a) {
	    
	    sendOutgoingCommands(a);

	}
		
    private void sendOutgoingCommands(int activePipe) {
   
        int x = 0;
        
        while ( hasReleaseCountRemaining(activePipe) 
        		&& PipeReader.hasContentToRead(fromCommandChannels[activePipe])
        		&& !connectionBlocker.isBlocked(Pipe.peekInt(fromCommandChannels[activePipe], 1)) //peek next address and check that it is not blocking for some time 
        		&& PipeReader.tryReadFragment(fromCommandChannels[activePipe])){

            x++;
            
        	long now = System.currentTimeMillis();
        	connectionBlocker.releaseBlocks(now);

        	assert(PipeReader.isNewMessage(fromCommandChannels [activePipe])) : "This test should only have one simple message made up of one fragment";
        	int msgIdx = PipeReader.getMsgIdx(fromCommandChannels [activePipe]);

        	switch(msgIdx){
            	case I2CCommandSchema.MSG_COMMAND_7:
            	{
            		int addr = PipeReader.readInt(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12);
    
            		byte[] backing = PipeReader.readBytesBackingArray(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int len  = PipeReader.readBytesLength(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int pos = PipeReader.readBytesPosition(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
            		int mask = PipeReader.readBytesMask(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);

            		assert(!connectionBlocker.isBlocked(addr)): "expected command to not be blocked";
    
              		Pipe.copyBytesFromToRing(backing, pos, mask, workingBuffer, 0, Integer.MAX_VALUE, len);
              		
              		boolean timeDigitalOns = false;
              		if (timeDigitalOns) {
                  		if (1 == workingBuffer[3]) {
                      		long now2 = System.nanoTime();
                      		if (time!=0) {
                      		    long duration = now2-time;
                      		    System.out.println("ns "+duration );
                      		}              		
                      		time = now2;
                  		}
              		}
              		
              		timeOut = System.currentTimeMillis() + writeTime;
            		while(!i2c.write((byte) addr, workingBuffer, len)&& System.currentTimeMillis()<timeOut){};
            	}                                      
            	break;
    
            	case I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20:
            	{  
            		int addr = PipeReader.readInt(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_ADDRESS_12);
            		long duration = PipeReader.readLong(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCKCONNECTIONMS_20_FIELD_DURATION_13);
            		connectionBlocker.until(addr, System.currentTimeMillis() + duration);
            		logger.info("I2C addr {} blocked for {} millis", addr, duration);
            	}   
            	break;
            	
                case I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21:
                {  
                    int addr = PipeReader.readInt(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12);
                    long time = PipeReader.readLong(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14);
                    connectionBlocker.until(addr, time);
                    logger.debug("I2C addr {} blocked until {} millis", addr, time);
                }
                
            	break;    
    
            	default:
    
            		System.out.println("Wrong Message index "+msgIdx);
            		assert(msgIdx == -1);
            		requestShutdown();      

        	}
        	PipeReader.releaseReadLock(fromCommandChannels [activePipe]);

        	//only do now after we know its not blocked and was completed
        	decReleaseCount(activePipe);

        }

        
    }



}