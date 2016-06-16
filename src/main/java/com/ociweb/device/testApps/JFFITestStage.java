package com.ociweb.device.testApps;

import static com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager.lookupFieldLocator;
import static com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager.lookupTemplateLocator;
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

import com.ociweb.device.config.GroveConnectionConfiguration;
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


public class JFFITestStage extends PronghornStage {

	private final Pipe toConfig;
	private final Pipe fromConfig;

	private final FieldReferenceOffsetManager FROMToConfig;
	private final DataOutputBlobWriter writer;

	private final FieldReferenceOffsetManager FROMFromJFFI;
	private final DataInputBlobReader reader;

	private static final Logger logger = LoggerFactory.getLogger(JFFITestStage.class);

	private static final I2CNativeLinuxBacking i2c = new I2CNativeLinuxBacking();

	public JFFITestStage(GraphManager graphManager, Pipe<RawDataSchema> fromConfig, Pipe<RawDataSchema> toConfig) {
		super(graphManager, NONE, toConfig);

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.toConfig = toConfig;
		this.fromConfig = fromConfig;
		this.FROMToConfig = Pipe.from(toConfig);
		this.writer = new DataOutputBlobWriter(toConfig);
		this.FROMFromJFFI = Pipe.from(fromConfig);
		this.reader = new DataInputBlobReader(fromConfig);

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
	public void run() { //message: {address, package size, bytes to be read, package[]}
		byte addr = 0x00;
		byte readBytes = 0x00;
		byte data[] = {};
		while (PipeReader.tryReadFragment(fromConfig)) {		

			assert(PipeReader.isNewMessage(fromConfig)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromConfig);
			
			if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
				reader.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				try {
					addr = reader.readByte();
					data = new byte[reader.readByte()];
					readBytes = reader.readByte();
					for (int i = 0; i < data.length; i++) {
						data[i]=reader.readByte();
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}

			i2c.write(addr, data);

			try {
				reader.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			PipeReader.releaseReadLock(fromConfig);
		} 
		
		if(readBytes>0){
			byte[] readData = i2c.read(addr, readBytes);
			while (tryWriteFragment(toConfig, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {
				DataOutputBlobWriter.openField(writer);
				try {
					for (int i = 0; i < readData.length; i++) {
						writer.writeByte(readData[i]);
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

				DataOutputBlobWriter.closeHighLevelField(writer, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
				publishWrites(toConfig);
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