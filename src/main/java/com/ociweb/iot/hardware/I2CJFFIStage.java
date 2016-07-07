package com.ociweb.iot.hardware;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.impl.Util;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class I2CJFFIStage extends AbstractOutputStage {

	private static I2CNativeLinuxBacking i2c;
	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	private final  Pipe<I2CResponseSchema> i2cResponsePipe;


	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);

	private int currentPoll = 0;
	private byte[][] inputs = null;
	private int scriptConn[] = null;
	private int scriptTask[] = null;
	private byte scriptMsg[][] = null;
	private int activeSize;


	public I2CJFFIStage(GraphManager graphManager, 
			Pipe<TrafficReleaseSchema>[] goPipe, 
			Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			Pipe<TrafficAckSchema>[] ackPipe, 
			Pipe<I2CResponseSchema> i2cResponsePipe,
			Hardware hardware) { 
		super(init(graphManager,hardware), hardware, i2cPayloadPipes, goPipe, join(ackPipe) /*add i2cREsponsePipe here*/); 
		this.fromCommandChannels = i2cPayloadPipes;
		this.i2cResponsePipe = i2cResponsePipe;
	}

	//this odd hack is here so we throw an error BEFORE calling supper and registering with the graph manger.
	private static GraphManager init(GraphManager gm, Hardware hardware) {	    
		I2CJFFIStage.i2c = new I2CNativeLinuxBacking(hardware.getI2CConnector());	    
		return gm;
	}

	@Override
	public void startup(){
		super.startup();
		//TODO: add rotary encoder support
		inputs = hardware.getGroveI2CInputs();
		activeSize = inputs.length;
		scriptConn = new int[activeSize];
		scriptTask = new int[activeSize];
		scriptMsg = new byte[activeSize][]; 
		for (int i = 0; i < activeSize; i++) {
			scriptConn[i] = inputs[i][0];
			scriptTask[i] = inputs[i][0];
			scriptMsg[i] = Arrays.copyOfRange(inputs[i], 2, inputs[i].length-1); 
		}

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

		//TODO: Alex, use a boolean to disable the "while" when we are waiting for a response.
		///     Ask the hardware for which addresses to listen to (but how to know how to talk? and how fast to poll?)
		//      get the data and send it out to the SINGULAR i2cResponsePipe pipe (see graph, a splitter shares the data as needed)

		if(currentPoll ==0){
			currentPoll++;
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
					I2CJFFIStage.i2c.write((byte) addr, buffer);
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
					I2CJFFIStage.i2c.write(cmdAddr, buffer);

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
			System.out.println(currentPoll);
			i2c.write((byte)scriptConn[currentPoll-1], scriptMsg[currentPoll-1]);


		}else{
			byte[] temp =i2c.read((byte)scriptConn[currentPoll-1], scriptTask[currentPoll-1]);

			if ((temp[0]!=-1 || temp.length!=1)&& PipeWriter.tryWriteFragment(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10)) { 
				PipeWriter.writeInt(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11, scriptConn[currentPoll-1]);
				PipeWriter.writeBytes(i2cResponsePipe, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12, temp);
				PipeWriter.publishWrites(i2cResponsePipe);

				currentPoll=currentPoll++%(activeSize+1);
				if(currentPoll!=0) i2c.write((byte)scriptConn[currentPoll-1], scriptMsg[currentPoll-1]);
			}


		}
	}



}