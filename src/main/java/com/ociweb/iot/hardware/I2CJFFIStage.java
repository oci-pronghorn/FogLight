package com.ociweb.iot.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class I2CJFFIStage extends AbstractOutputStage {

	private final I2CBacking i2c;
	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	private final  Pipe<I2CResponseSchema> i2cResponsePipe;


	private static final Logger logger = LoggerFactory.getLogger(I2CJFFIStage.class);

	private int currentPoll = 0;
	private I2CConnection[] inputs = null;
	private int lastWriteIdx = 0;

	public I2CJFFIStage(GraphManager graphManager, 
			Pipe<TrafficReleaseSchema>[] goPipe, 
			Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			Pipe<TrafficAckSchema>[] ackPipe, 
			Pipe<I2CResponseSchema> i2cResponsePipe,
			Hardware hardware) { 
		super(graphManager, hardware, i2cPayloadPipes, goPipe, join(ackPipe) /*add i2cREsponsePipe here*/); 
		this.i2c = hardware.i2cBacking;
		this.fromCommandChannels = i2cPayloadPipes;
		this.i2cResponsePipe = i2cResponsePipe;
		
		this.inputs = null==hardware.i2cInputs?new I2CConnection[0]:hardware.i2cInputs;
	}

    @Override
	public void startup(){
		super.startup();
		//TODO: add rotary encoder support

		//        int j = config.maxAnalogMovingAverage()-1; //TODO: work out what this does
		//        movingAverageHistory = new int[j][]; 
		//        while (--j>=0) {
		//            movingAverageHistory[j] = new int[activeSize];            
		//        }
		//lastPublished = new int[activeSize];


		//for devices that must poll frequently
		//        frequentScriptConn = new int[activeSize];
		//        frequentScriptTwig = new GroveTwig[activeSize];
		//        frequentScriptLastPublished = new int[activeSize];

		//before we setup the pins they must start in a known state
		//this is required for the ATD converters (eg any analog port usage)


		//		byte sliceCount = 0;

		//configure each sensor


		//		int i;
		//
		//		i = hardware.getGroveI2CInputs().length;
		//		while (--i>=0) {
		//
		//			int idx = Util.reverseBits(sliceCount++);
		//			scriptConn[idx] = config.analogInputs[i].connection;
		//			scriptTask[idx] = DO_INT_READ;
		//			scriptTwig[idx] = config.analogInputs[i].twig;
		//			System.out.println("configured "+config.analogInputs[i].twig+" on connection "+config.analogInputs[i].connection);
		//
		//		}
		//
		//		if (sliceCount>=16) {
		//			throw new UnsupportedOperationException("The grove base board does not support this many connections.");
		//		}
	}

	protected void processMessagesForPipe(int a) {

		if(currentPoll ==0){
			currentPoll++;
			lastWriteIdx = -1; // Resets idx for polling below
			while (hasReleaseCountRemaining(a) 
					&& Pipe.hasContentToRead(fromCommandChannels[activePipe])
					&& !connectionBlocker.isBlocked(Pipe.peekInt(fromCommandChannels[activePipe], 1)) //peek next address and check that it is not blocking for some time                
					&& PipeReader.tryReadFragment(fromCommandChannels[activePipe])){

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


					byte[] buffer = new byte[len];
					Pipe.copyBytesFromToRing(backing, pos, mask, buffer, 0, Integer.MAX_VALUE, len);

					System.out.print("Sent ");
					for (int i = 0; i < buffer.length; i++) {
						System.out.print(buffer[i]+", ");
					}
					System.out.println(" To "+addr);
					i2c.write((byte) addr, buffer);
				}                                      
				break;

				case I2CCommandSchema.MSG_BLOCK_10:
				{  
					int addr = PipeReader.readInt(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCK_10_FIELD_ADDRESS_12);
					long duration = PipeReader.readLong(fromCommandChannels [activePipe], I2CCommandSchema.MSG_BLOCK_10_FIELD_DURATION_13);
					System.out.println("adding block for "+addr+" for "+duration);
					connectionBlocker.until(addr, System.currentTimeMillis() + duration);
				}   
				break;    

				case I2CCommandSchema.MSG_COMMANDANDBLOCK_11:
				{
					byte[] backing = PipeReader.readBytesBackingArray(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMANDANDBLOCK_11_FIELD_BYTEARRAY_2);
					int len  = PipeReader.readBytesLength(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMANDANDBLOCK_11_FIELD_BYTEARRAY_2);
					int pos = PipeReader.readBytesPosition(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMANDANDBLOCK_11_FIELD_BYTEARRAY_2);
					int mask = PipeReader.readBytesMask(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMANDANDBLOCK_11_FIELD_BYTEARRAY_2);

					byte cmdAddr = backing[mask&pos++]; //TODO: this will eventually be in its own field
					int payloadSize = backing[mask&pos++];//TODO: this is a VERY bad idea since payload can only be 127 bytes and we know they are often longer!!
					//the above size is also not needed
					int expectedPayloadSize = len-2;
					assert(payloadSize == expectedPayloadSize);
					byte[] buffer = new byte[expectedPayloadSize];
					Pipe.copyBytesFromToRing(backing, pos, mask, buffer, 0, Integer.MAX_VALUE, expectedPayloadSize);

					System.out.print("Sent ");
					for (int i = 0; i < buffer.length; i++) {
						System.out.print(buffer[i]+", ");
					}
					System.out.println(" To "+cmdAddr);
					i2c.write(cmdAddr, buffer);

					long duration = PipeReader.readLong(fromCommandChannels [activePipe], I2CCommandSchema.MSG_COMMANDANDBLOCK_11_FIELD_DURATION_13);

					connectionBlocker.until(cmdAddr, System.currentTimeMillis() + duration);
				}   
				break;

				default:

					System.out.println("Wrong Message index "+msgIdx);
					assert(msgIdx == -1);
					requestShutdown();      

				}
				PipeReader.releaseReadLock(fromCommandChannels [activePipe]);

				//only do now after we know its not blocked and was completed
				decReleaseCount(a);

			}
			


		}else{
		    int tempIdx = currentPoll-1;
		    if (tempIdx<inputs.length) {
    			byte[] temp =i2c.read(inputs[tempIdx].address, inputs[tempIdx].readBytes);
    			System.out.print("I2C Read ");
    			for (int i = 0; i < temp.length; i++) {
					System.out.print(temp[i] + " ");
				}
    			System.out.println("");
    			if ((temp[0]!=-1 || temp.length!=1)&& PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
    				PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, inputs[tempIdx].address);
    				PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, temp);
    				PipeWriter.writeLong(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13, System.nanoTime());
    				PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14, inputs[tempIdx].register);
    				PipeWriter.publishWrites(i2cResponsePipe);
    				System.out.println("I2C read on "+inputs[tempIdx].register);
    				System.out.println("Sent "+temp[0]+" to listener");
    
    				currentPoll=currentPoll++%(inputs.length+1);
    			}
		    }


		}

		//Only send one read command for each i2c read
		if (lastWriteIdx<currentPoll-1) {
			lastWriteIdx = currentPoll-1;
		    I2CConnection connection = inputs[lastWriteIdx];
		    i2c.write((byte)connection.address, connection.readCmd);
		    
		}
	}



}