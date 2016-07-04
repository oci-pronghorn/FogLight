package com.ociweb.iot.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class I2CJFFIStage extends AbstractOutputStage {

    private static I2CNativeLinuxBacking i2c;
	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	
	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);


	public I2CJFFIStage(GraphManager graphManager, 
	                    Pipe<TrafficReleaseSchema>[] goPipe, 
	                    Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			            Pipe<TrafficAckSchema>[] ackPipe, 
			          //add Pipe<RawDataSchema>[] i2cResponsePipe,
			            Hardware hardware) { 
		super(init(graphManager,hardware), hardware, i2cPayloadPipes, goPipe, join(ackPipe) /*add i2cREsponsePipe here*/); 
		this.fromCommandChannels = i2cPayloadPipes;

	}
	
	//this odd hack is here so we throw an error BEFORE calling supper and registering with the graph manger.
	private static GraphManager init(GraphManager gm, Hardware hardware) {	    
	    I2CJFFIStage.i2c = new I2CNativeLinuxBacking(hardware.getI2CConnector());	    
	    return gm;
	}


    protected void processMessagesForPipe(int a) {
        
        
        while (hasReleaseCountRemaining(a) 
                && Pipe.hasContentToRead(fromCommandChannels[activePipe])
                && !connectionBlocker.isBlocked(Pipe.peekInt(fromCommandChannels[activePipe], 1)) //peek next address and check that it is not blocking for some time                
                && PipeReader.tryReadFragment(fromCommandChannels[activePipe]) ){
  
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
                    
                    byte cmdAddr = backing[mask&pos++]; //TODO: will remove redundant information soon
                    assert(addr==cmdAddr);
                    assert(!connectionBlocker.isBlocked(cmdAddr)): "expected command to not be blocked";
                    
                    int payloadSize = backing[mask&pos++];//TODO: this is a VERY bad idea since payload can only be 127 bytes and we know they are often longer!!
                    //the above size is also not needed
                    int expectedPayloadSize = len-2;
                    assert(payloadSize == expectedPayloadSize);
                    byte[] buffer = new byte[expectedPayloadSize];
                    Pipe.copyBytesFromToRing(backing, pos, mask, buffer, 0, Integer.MAX_VALUE, expectedPayloadSize);
                                   
                    I2CJFFIStage.i2c.write(cmdAddr, buffer);
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
    }
    

//		//		for (int i = 0; i < this.hardware.digitalInputs.length; i++) { //TODO: This polls every attached input, are there intermittent inputs?
//		//			if(this.hardware.digitalInputs[i].type.equals(ConnectionType.GrovePi)){
//		//				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
//		//					DataOutputBlobWriter.openField(writeListener);
//		//					try {
//		//						byte[] tempData = {};
//		//						byte[] message = {0x01, 0x01, hardware.digitalInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
//		//						i2c.write((byte) 0x04, message);
//		//						while(tempData.length == 0){ //TODO: Blocking call
//		//							i2c.read(hardware.digitalInputs[i].connection, 1);
//		//						}
//		//						writeListener.write(tempData); //TODO: Use some other Schema
//		//					} catch (IOException e) {
//		//						logger.error(e.getMessage(), e);
//		//					}
//		//
//		//					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
//		//					publishWrites(toListener);
//		//				}else{
//		//					System.out.println("unable to write fragment");
//		//				}
//		//			}	
//		//		}
//		//		for (int i = 0; i < this.hardware.analogInputs.length; i++) {
//		//			if(this.hardware.analogInputs[i].type.equals(ConnectionType.GrovePi)){
//		//				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
//		//					DataOutputBlobWriter.openField(writeListener);
//		//					try {
//		//						byte[] tempData = {};
//		//						byte[] message = {0x01, 0x03, hardware.analogInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
//		//						i2c.write((byte) 0x04, message);
//		//						while(tempData.length == 0){ //TODO: Blocking call
//		//							i2c.read(hardware.digitalInputs[i].connection, 1);
//		//						}
//		//						writeListener.write(tempData); //TODO: Use some other Schema
//		//					} catch (IOException e) {
//		//						logger.error(e.getMessage(), e);
//		//					}
//		//
//		//					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
//		//					publishWrites(toListener);
//		//				}else{
//		//					System.out.println("unable to write fragment");
//		//				}
//		//			}	
//		//		}






}