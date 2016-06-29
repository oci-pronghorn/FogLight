package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;
import static com.ociweb.pronghorn.pipe.PipeWriter.writeASCII;
import static com.ociweb.pronghorn.pipe.PipeWriter.writeDecimal;
import static com.ociweb.pronghorn.pipe.PipeWriter.writeLong;
import static com.ociweb.pronghorn.pipe.PipeWriter.writeUTF8;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.iot.schema.AcknowledgeSchema;
import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class AnalogDigitalOutputStage extends PronghornStage {
	
	private final Pipe<GroveRequestSchema> fromCommandChannel;
	private final Pipe<GoSchema> goPipe;
	private final Pipe<AcknowledgeSchema> ackPipe;
	private DataOutputBlobWriter<AcknowledgeSchema> writeAck;
	private DataInputBlobReader<GroveRequestSchema> readCommandChannel;
	private DataInputBlobReader<GoSchema> readGo;

	private Hardware hardware;
	private int goCount;
	private int connector;//should be passed in first
	private int value;

	private static final Logger logger = LoggerFactory.getLogger(AnalogDigitalOutputStage.class);

	public AnalogDigitalOutputStage(GraphManager graphManager, Pipe<GroveRequestSchema> ccToAdOut,Pipe<GoSchema> goPipe,
			Pipe<AcknowledgeSchema> ackPipe, Hardware hardware) {
	
		super(graphManager, join(goPipe, ccToAdOut),ackPipe);
		////////
		// STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.hardware = hardware;
		this.ackPipe = ackPipe;
		this.fromCommandChannel = ccToAdOut;
		this.goPipe = goPipe;
	}
	
	
	@Override
	public void startup() {
		try{
			this.writeAck =           new DataOutputBlobWriter<AcknowledgeSchema>(ackPipe);
			this.readCommandChannel = new DataInputBlobReader<GroveRequestSchema>(fromCommandChannel);
			this.readGo =             new DataInputBlobReader(goPipe);
			this.connector = 0;
			this.goCount = 0;
			this.value = 0;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
		for (int i = 0; i < hardware.digitalOutputs.length; i++) {
			if(hardware.digitalOutputs[i].type.equals(ConnectionType.Direct))hardware.configurePinsForDigitalOutput(hardware.digitalOutputs[i].connection);
		}
		for (int i = 0; i < hardware.pwmOutputs.length; i++) {
			if(hardware.pwmOutputs[i].type.equals(ConnectionType.Direct)) hardware.configurePinsForAnalogOutput(hardware.pwmOutputs[i].connection);
		}
		// need to change to make the Edison PIN to startup correctly
		super.startup();
		//call the super.startup() last to keep schedulers from getting too eager and starting early
	}

	@Override
	public void run() { //message: {address, package size, bytes to be read, package[]}
		goCount =0;
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
		
	while (goCount > 0 && PipeReader.tryReadFragment(fromCommandChannel)){
			assert(PipeReader.isNewMessage(fromCommandChannel)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromCommandChannel);
			
			if (RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
				readCommandChannel.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					for (int i = 0; i < hardware.digitalOutputs.length; i++) {
					if(hardware.digitalOutputs[i].type.equals(ConnectionType.Direct)){
					try {
						connector = readCommandChannel.readInt();
						value = 	readCommandChannel.readInt();
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					hardware.digitalWrite(connector, value);
						}
				}
					for (int i = 0; i < hardware.pwmOutputs.length; i++) {
					if(hardware.pwmOutputs[i].type.equals(ConnectionType.Direct)){	
					try {
						connector = readCommandChannel.readInt();
						value       = readCommandChannel.readInt();
					} catch (IOException e2) {
						e2.printStackTrace();
					} 
					hardware.digitalWrite(connector, value);
						}
					}
			}
			else{
					assert(msgIdx == -1): "The message is not -1 but it will still shut down";
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