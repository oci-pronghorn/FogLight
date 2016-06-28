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
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;

public class I2CJFFIStage extends PronghornStage {

	private final Pipe<I2CCommandSchema> fromCommandChannel;
	private final Pipe<RawDataSchema> toListener;
	private final Pipe<GoSchema> goPipe; //TODO: Change the Schema
	private final Pipe<AcknowledgeSchema> ackPipe;
	

	private DataOutputBlobWriter<RawDataSchema> writeListener;
	private DataOutputBlobWriter<AcknowledgeSchema> writeAck;
	private DataInputBlobReader<I2CCommandSchema> readCommandChannel;
	private DataInputBlobReader<GoSchema> readGo;

	private Hardware hardware;
	private int goCount;
	private byte addr;
	private byte[] data;
	


	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);

	private static I2CNativeLinuxBacking i2c;

	public I2CJFFIStage(GraphManager graphManager, Pipe<GoSchema> goPipe, Pipe<I2CCommandSchema> i2cPayloadPipe, 
			Pipe<AcknowledgeSchema> ackPipe, Pipe<RawDataSchema> toListener, Hardware hardware) {
		super(graphManager, join(goPipe, i2cPayloadPipe), join(ackPipe, toListener)); 

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.toListener = toListener;
		this.ackPipe = ackPipe;
		this.fromCommandChannel = i2cPayloadPipe;
		this.goPipe = goPipe;

		this.writeListener = new DataOutputBlobWriter<RawDataSchema>(toListener);
		this.writeAck = new DataOutputBlobWriter<AcknowledgeSchema>(ackPipe);
		this.readCommandChannel = new DataInputBlobReader<I2CCommandSchema>(i2cPayloadPipe);
		this.readGo = new DataInputBlobReader<GoSchema>(goPipe);
		I2CJFFIStage.i2c = new I2CNativeLinuxBacking((byte)1); //TODO: get device spec from Hardware
		this.hardware = hardware;

		this.goCount = 0;
		this.data = new byte[0]; 
		this.addr = 0;
	}


	@Override
	public void startup() {

		try{


		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		//call the super.startup() last to keep schedulers from getting too eager and starting early
		super.startup();
	}


	@Override
	public void run() { 
		goCount = 0;
		while (PipeReader.tryReadFragment(goPipe)) {		
			assert(PipeReader.isNewMessage(goPipe)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(goPipe);

			if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
				readGo.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				try {
					this.goCount += readGo.readByte();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				try {
					readGo.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}else{
				assert(msgIdx == -1);
				requestShutdown();
			}


			PipeReader.releaseReadLock(goPipe);
		} 

		while (goCount>0) {
			if(PipeReader.tryReadFragment(fromCommandChannel)) {
				assert(PipeReader.isNewMessage(fromCommandChannel)) : "This test should only have one simple message made up of one fragment";
				int msgIdx = PipeReader.getMsgIdx(fromCommandChannel);

				if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
					readCommandChannel.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					try {
						addr = readCommandChannel.readByte(); //TODO: Use I2CCommandSchema
						data = new byte[readCommandChannel.readByte()]; //TODO: Nathan take out the trash
						for (int i = 0; i < data.length; i++) {
							data[i]=readCommandChannel.readByte();
						}
						I2CJFFIStage.i2c.write(addr, data);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
					try {
						readCommandChannel.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}else{
					assert(msgIdx == -1);
					requestShutdown();
				}


				PipeReader.releaseReadLock(fromCommandChannel);
				
				if (tryWriteFragment(ackPipe, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Use acknowledgeSchema
					DataOutputBlobWriter.openField(writeAck);
					try {
						writeAck.write(1);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}

					DataOutputBlobWriter.closeHighLevelField(writeAck, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					publishWrites(ackPipe);
				}else{
					System.out.println("unable to write fragment");
				}
				
				goCount--;
			}
		} 
		for (int i = 0; i < this.hardware.digitalInputs.length; i++) { //TODO: This polls every attached input, are there intermittent inputs?
			if(this.hardware.digitalInputs[i].type.equals(ConnectionType.GrovePi)){
				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
					DataOutputBlobWriter.openField(writeListener);
					try {
						byte[] tempData = {};
						byte[] message = {0x01, 0x01, hardware.digitalInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
						i2c.write((byte) 0x04, message);
						while(tempData.length == 0){ //TODO: Blocking call
								i2c.read(hardware.digitalInputs[i].connection, 1);
						}
						writeListener.write(tempData); //TODO: Use some other Schema
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}

					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					publishWrites(toListener);
				}else{
					System.out.println("unable to write fragment");
				}
			}	
		}
		for (int i = 0; i < this.hardware.analogInputs.length; i++) {
			if(this.hardware.analogInputs[i].type.equals(ConnectionType.GrovePi)){
				if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) { //TODO: Do we want to open and close pipe writer for every poll?
					DataOutputBlobWriter.openField(writeListener);
					try {
						byte[] tempData = {};
						byte[] message = {0x01, 0x03, hardware.analogInputs[i].connection, 0x00, 0x00};//TODO: This is GrovePi specific. Should it be in hardware?
						i2c.write((byte) 0x04, message);
						while(tempData.length == 0){ //TODO: Blocking call
								i2c.read(hardware.digitalInputs[i].connection, 1);
						}
						writeListener.write(tempData); //TODO: Use some other Schema
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}

					DataOutputBlobWriter.closeHighLevelField(writeListener, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					publishWrites(toListener);
				}else{
					System.out.println("unable to write fragment");
				}
			}	
		}
		
		
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