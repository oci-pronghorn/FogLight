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


public class JFFIStage extends PronghornStage {

	private final Pipe<RawDataSchema> toHardware;
	private final Pipe<RawDataSchema> fromHardware;

	private DataOutputBlobWriter<RawDataSchema> writer;

	private DataInputBlobReader<RawDataSchema> reader;

	private static final Logger logger = LoggerFactory.getLogger(JFFIStage.class);

	private static I2CNativeLinuxBacking i2c;

	public JFFIStage(GraphManager graphManager, Pipe<RawDataSchema> toHardware, Pipe<RawDataSchema> fromHardware) {
		super(graphManager, toHardware, fromHardware);

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.toHardware = toHardware;
		this.fromHardware = fromHardware;
		this.writer = null;
		this.reader = null;
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
		System.out.println("JFFI Stage setup successful");
	}

	
	private DataOutputBlobWriter<RawDataSchema> getWriter(){
    	//assert Pipe.isInit(toHardware);
		if(null == writer){
    		this.writer = new DataOutputBlobWriter<RawDataSchema>(toHardware);
    	}
    	return writer;
    }
	private DataInputBlobReader<RawDataSchema> getReader(){
    	if(null == reader){
    		this.reader = new DataInputBlobReader<RawDataSchema>(fromHardware);
    	}
    	return reader;
    }

	@Override
	public void run() { //message: {address, package size, bytes to be read, package[]}
		byte addr = 0x00;
		byte readBytes = 0x00;
		byte data[] = {};
		DataOutputBlobWriter<RawDataSchema> writer = getWriter();
		DataInputBlobReader<RawDataSchema> reader = getReader();
		while (PipeReader.tryReadFragment(fromHardware)) {		

			assert(PipeReader.isNewMessage(fromHardware)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromHardware);
			
			if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
				reader.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				try {
					addr = reader.readByte();
					data = new byte[reader.readByte()]; //TODO: Nathan take out the trash
					readBytes = reader.readByte();
					for (int i = 0; i < data.length; i++) {
						data[i]=reader.readByte();
					}
					this.i2c.write(addr, data);
					System.out.println("JFFI Stage writes");
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}else{
				assert(msgIdx == -1);
				requestShutdown();
			}
			

			PipeReader.releaseReadLock(fromHardware);
		} 
		
		if(readBytes>0){
			byte[] readData = i2c.read(addr, readBytes);
			System.out.println("i2c Read");
			while (tryWriteFragment(toHardware, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {
				DataOutputBlobWriter.openField(writer);
				try {
					writer.write(readData);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

				DataOutputBlobWriter.closeHighLevelField(writer, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				publishWrites(toHardware);
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