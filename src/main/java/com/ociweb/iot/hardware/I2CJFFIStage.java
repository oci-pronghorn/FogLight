package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.AcknowledgeSchema;
import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;

public class I2CJFFIStage extends PronghornStage {

	private final Pipe<I2CCommandSchema>[] fromCommandChannels;
	//private final Pipe<RawDataSchema> toListener;
	private final Pipe<GoSchema> goPipe; //TODO: Change the Schema
	private final Pipe<AcknowledgeSchema> ackPipe;


	//private DataOutputBlobWriter<RawDataSchema> writeListener;
	private DataOutputBlobWriter<AcknowledgeSchema> writeAck;
	private DataInputBlobReader<I2CCommandSchema>[] readCommandChannels;
	private DataInputBlobReader<GoSchema> readGo;

	private Hardware hardware;
	private int goCount;
	private int ackCount;
	private int pipeIdx;
	private byte addr;
	private byte[] data;



	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);

	private static I2CNativeLinuxBacking i2c;

	public I2CJFFIStage(GraphManager graphManager, Pipe<GoSchema> goPipe, Pipe<I2CCommandSchema>[] i2cPayloadPipes, 
			Pipe<AcknowledgeSchema> ackPipe, Hardware hardware) { //add an I2CListenerPipe
		super(graphManager, join(join(goPipe), i2cPayloadPipes), join(ackPipe)); //TODO: does double join work? feels weird.

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		//this.toListener = toListener;
		this.ackPipe = ackPipe;
		this.fromCommandChannels = i2cPayloadPipes;
		this.goPipe = goPipe;
		this.hardware = hardware;

		//this.writeListener = new DataOutputBlobWriter<RawDataSchema>(toListener);


	}


	@Override
	public void startup() {
		try{
			this.readCommandChannels = new DataInputBlobReader[fromCommandChannels.length];
			System.out.println(fromCommandChannels.length+" Channels into I2CJFFIStage");
			for (int i = 0; i < readCommandChannels.length; i++) {
				this.readCommandChannels[i] = new DataInputBlobReader<I2CCommandSchema>(this.fromCommandChannels[i]);
			}
			I2CJFFIStage.i2c = new I2CNativeLinuxBacking(hardware.getI2CConnector()); 
			this.goCount = 0;
			this.ackCount = 0;
			this.pipeIdx = 0;
			this.data = new byte[0]; 
			this.addr = 0;

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		//call the super.startup() last to keep schedulers from getting too eager and starting early
		super.startup();
	}


	@Override
	public void run() { 
		System.out.println("You are actually working");
		while (PipeReader.tryReadFragment(goPipe)) {		
			assert(PipeReader.isNewMessage(goPipe)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(goPipe);

			if(GoSchema.MSG_RELEASE_20== msgIdx){
				assert(goCount>=0);
				goCount += PipeReader.readInt(goPipe, GoSchema.MSG_RELEASE_20_FIELD_COUNT_22);
				//pipeIdx = PipeReader.readInt(goPipe, GoSchema.MSG_GO_10_FIELD_PIPEIDX_11);
				pipeIdx = 0;
				ackCount = goCount;
				System.out.println("GoCount "+goCount);
				assert(goCount>0);
			}else{
				assert(msgIdx == -1);
				requestShutdown();
			}
			PipeReader.releaseReadLock(goPipe);
		} 
		
		while (goCount>0 && PipeReader.tryReadFragment(fromCommandChannels[pipeIdx])) {
			goCount--;
			assert(PipeReader.isNewMessage(fromCommandChannels[pipeIdx])) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromCommandChannels[pipeIdx]);
			System.out.println("Inside the while");
			if(I2CCommandSchema.MSG_COMMAND_1 == msgIdx){
				readCommandChannels[pipeIdx].openHighLevelAPIField(I2CCommandSchema.MSG_COMMAND_1_FIELD_BYTEARRAY_2);
				try {
					addr = readCommandChannels[pipeIdx].readByte(); 
					data = new byte[readCommandChannels[pipeIdx].readByte()]; //TODO: Nathan take out the trash. ReadByteArray method?
					for (int i = 0; i < data.length; i++) {
						data[i]=readCommandChannels[pipeIdx].readByte();
					}
					I2CJFFIStage.i2c.write(addr, data);
					System.out.println("writing to I2C bus");
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					System.out.println("I failed");
				}
				try {
					readCommandChannels[pipeIdx].close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					System.out.println("I failed to close");
				}
			}else{
				System.out.println("Wrong Message index "+msgIdx);
				assert(msgIdx == -1);
				requestShutdown();
			}

			PipeReader.releaseReadLock(fromCommandChannels[pipeIdx]);
			
			
			
			if(goCount==0 && ackCount!=0){
				if (tryWriteFragment(ackPipe, AcknowledgeSchema.MSG_DONE_10)) {
					PipeWriter.writeInt(ackPipe, AcknowledgeSchema.MSG_DONE_10, 0);
					publishWrites(ackPipe);
					ackCount = goCount;
					System.out.println("Send Acknowledgement");
				}else{
					System.out.println("unable to write fragment");
				}
			}
			
		}
		

		//		for (int i = 0; i < this.hardware.digitalInputs.length; i++) { //TODO: This polls every attached input, are there intermittent inputs?
		//			if(this.hardware.digitalInputs[i].type.equals(ConnectionType.GrovePi)){
		//				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
		//					DataOutputBlobWriter.openField(writeListener);
		//					try {
		//						byte[] tempData = {};
		//						byte[] message = {0x01, 0x01, hardware.digitalInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
		//						i2c.write((byte) 0x04, message);
		//						while(tempData.length == 0){ //TODO: Blocking call
		//							i2c.read(hardware.digitalInputs[i].connection, 1);
		//						}
		//						writeListener.write(tempData); //TODO: Use some other Schema
		//					} catch (IOException e) {
		//						logger.error(e.getMessage(), e);
		//					}
		//
		//					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		//					publishWrites(toListener);
		//				}else{
		//					System.out.println("unable to write fragment");
		//				}
		//			}	
		//		}
		//		for (int i = 0; i < this.hardware.analogInputs.length; i++) {
		//			if(this.hardware.analogInputs[i].type.equals(ConnectionType.GrovePi)){
		//				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
		//					DataOutputBlobWriter.openField(writeListener);
		//					try {
		//						byte[] tempData = {};
		//						byte[] message = {0x01, 0x03, hardware.analogInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
		//						i2c.write((byte) 0x04, message);
		//						while(tempData.length == 0){ //TODO: Blocking call
		//							i2c.read(hardware.digitalInputs[i].connection, 1);
		//						}
		//						writeListener.write(tempData); //TODO: Use some other Schema
		//					} catch (IOException e) {
		//						logger.error(e.getMessage(), e);
		//					}
		//
		//					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		//					publishWrites(toListener);
		//				}else{
		//					System.out.println("unable to write fragment");
		//				}
		//			}	
		//		}


	}



	@Override
	public void shutdown() {
		//if batching was used this will publish any waiting fragments
		//RingBuffer.publishAllWrites(output);

		try{

			///////
			//PUT YOUR LOGIC HERE TO CLOSE CONNECTIONS FROM THE DATABASE OR OTHER SOURCE OF INFORMATION
			//////

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}





}