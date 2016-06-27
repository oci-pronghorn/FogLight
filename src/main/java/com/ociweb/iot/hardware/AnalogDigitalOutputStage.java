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
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class AnalogDigitalOutputStage extends PronghornStage {
	private final Pipe<RawDataSchema> fromCommandChannel;
	private final Pipe<RawDataSchema> goPipe; 
	private final Pipe<AcknowledgeSchema> ackPipe;
	private final Pipe<AcknowledgeSchema> outPipe; //might need to change to 
	private final Pipe<RawDataSchema>[] inPipes;
	
	
	private DataOutputBlobWriter<AcknowledgeSchema> writeAck;
	private DataInputBlobReader<RawDataSchema> readCommandChannel;
	private DataInputBlobReader<RawDataSchema> readGo;
	
	private Hardware hardware;
	private int goCount;
	private byte addr;
	private byte packageSize;
	private byte[] data;
	private byte[][] connections;
	
	private static final Logger logger = LoggerFactory.getLogger(AnalogDigitalOutputStage.class);

	
	public AnalogDigitalOutputStage(GraphManager graphManager, Pipe<RawDataSchema>[] inPipes, Pipe<AcknowledgeSchema> outPipe,Hardware hardware) {
		super(graphManager, inPipes, outPipe); 
		
		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		
		this.inPipes = inPipes;
		this.outPipe= outPipe;
		this.hardware = hardware;
		assert (inPipes.length == 2);
		ackPipe = outPipe;
		this.fromCommandChannel = inPipes[0];
		this.goPipe = inPipes[1];	
		
		this.writeAck = new DataOutputBlobWriter(ackPipe);
		this.readCommandChannel = new DataInputBlobReader(fromCommandChannel);
		this.readGo = new DataInputBlobReader(goPipe);
		this.addr = 0x00;
		this.goCount = 0;
		this.data= new byte [0];
	}
	
	
	@Override
	public void startup() {

		try{

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		//call the super.startup() last to keep schedulers from getting too eager and starting early
		super.startup();
		// need to change to make the Edison PIN to startup correctly
		for (int i = 0; i < hardware.digitalOutputs.length; i++) {
			if(hardware.digitalOutputs[i].type.equals(ConnectionType.Direct)) hardware.configurePinsForDigitalInput(hardware.digitalOutputs[i].connection);
		}
		for (int i = 0; i < hardware.analogOu.length; i++) {
			if(hardware.analogInputs[i].type.equals(ConnectionType.Direct)) hardware.configurePinsForAnalogInput(hardware.analogInputs[i].connection);
		}

		hardware.beginPinConfiguration();
		hardware.endPinConfiguration();
		
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
	while (goCount > 0){
		if(PipeReader.tryReadFragment(fromCommandChannel)) {
			assert(PipeReader.isNewMessage(fromCommandChannel)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromCommandChannel);
			
			if (RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx)
	}
		
}
}