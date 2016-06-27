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

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;


public class I2CJFFIStage extends PronghornStage {

	private final Pipe<RawDataSchema> fromCommandChannel;
	private final Pipe<RawDataSchema> toListener;
	private final Pipe<RawDataSchema> goPipe; //TODO: Change the Schema
	private final Pipe<RawDataSchema> ackPipe;
	private final Pipe<RawDataSchema>[] outPipes;
	private final Pipe<RawDataSchema>[] inPipes;

	private DataOutputBlobWriter<RawDataSchema> writeListener;
	private DataOutputBlobWriter<RawDataSchema> writeAck;
	private DataInputBlobReader<RawDataSchema> readCommandChannel;
	private DataInputBlobReader<RawDataSchema> readGo;

	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);

	private static I2CNativeLinuxBacking i2c;

	public I2CJFFIStage(GraphManager graphManager, Pipe<RawDataSchema>[] inPipes, Pipe<RawDataSchema>[] outPipes) {
		super(graphManager, inPipes, outPipes); 

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.inPipes = inPipes;
		this.outPipes = outPipes;
		assert(inPipes.length == 2);
		assert(outPipes.length ==2);
		this.toListener = outPipes[0];
		this.ackPipe = outPipes[1];
		this.fromCommandChannel = inPipes[0];
		this.goPipe = inPipes[1];
		
		this.writeListener = new DataOutputBlobWriter(toListener);
		this.writeAck = new DataOutputBlobWriter(ackPipe);
		this.readCommandChannel = new DataInputBlobReader(fromCommandChannel);
		this.readGo = new DataInputBlobReader(goPipe);
		this.i2c = new I2CNativeLinuxBacking();

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

	
	private DataOutputBlobWriter<RawDataSchema> getWriter(){
    	//assert Pipe.isInit(writeListener);
		if(null == writeListener){
    		this.writeListener = new DataOutputBlobWriter<RawDataSchema>(toListener);
    	}
    	return writeListener;
    }
	private DataInputBlobReader<RawDataSchema> getReader(){
    	if(null == readCommandChannel){
    		this.readCommandChannel = new DataInputBlobReader<RawDataSchema>(fromCommandChannel);
    	}
    	return readCommandChannel;
    }

	@Override
	public void run() { //message: {address, package size, bytes to be read, package[]}
		byte addr = 0x00;
		byte readBytes = 0x00;
		byte data[] = {};
		DataOutputBlobWriter<RawDataSchema> writeListener = getWriter();
		DataInputBlobReader<RawDataSchema> readCommandChannel = getReader();
		while (PipeReader.tryReadFragment(fromCommandChannel)) {		

			assert(PipeReader.isNewMessage(fromCommandChannel)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromCommandChannel);
			
			if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
				readCommandChannel.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				try {
					addr = readCommandChannel.readByte();
					data = new byte[readCommandChannel.readByte()]; //TODO: Nathan take out the trash
					readBytes = readCommandChannel.readByte();
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
		} 
		
		if(readBytes>0){
			byte[] readData = i2c.read(addr, readBytes);
			byte[] temp = new byte[readData.length+1]; //TODO: Nathan take out the trash
			temp[0] = readBytes;
			for (int i = 1; i < temp.length; i++) {  
				temp[i] = readData[i-1];
				//System.out.print(temp[i] + " ");
			}
			//System.out.println("");
			//System.out.println("i2c Read");
			if (tryWriteFragment(toListener, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {
				DataOutputBlobWriter.openField(writeListener);
				try {
					writeListener.write(temp);
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