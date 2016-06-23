package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

/*
 * Stage supports JFFIStage by reading and writing i2c packets to it. Its methods are accessed by the implementation class
 */
public class JFFISupportStage extends PronghornStage {

	private DataOutputBlobWriter<RawDataSchema> writer;
	private DataInputBlobReader<RawDataSchema> reader;

	private Pipe<RawDataSchema> readPipe;
	private Pipe<RawDataSchema> writePipe;

	private static final Logger logger = LoggerFactory.getLogger(JFFISupportStage.class);

	private List<byte[]> dataFromPipe;
	private JFFIStage jffiStage;

	public JFFISupportStage(GraphManager graphManager, Pipe<RawDataSchema> readPipe, Pipe<RawDataSchema> writePipe) {
		super(graphManager, writePipe, readPipe);

		////////
		//STORE OTHER FIELDS THAT WILL BE REQUIRED IN STARTUP
		////////
		this.readPipe = readPipe;
		this.writePipe = writePipe;
		this.writer = null;
		this.reader = null;
		this.dataFromPipe = new ArrayList<byte[]>();

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

	}

	protected synchronized void writeData(byte[] message){
		this.writer = getWriter();
		if (tryWriteFragment(writePipe, RawDataSchema.MSG_CHUNKEDSTREAM_1)) {
			DataOutputBlobWriter.openField(writer);
			try {
				writer.write(message);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			DataOutputBlobWriter.closeHighLevelField(writer, RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
			publishWrites(writePipe);
		}else{
			System.out.println("unable to write fragment");
		}

	}

	protected synchronized byte[] readData(){
		this.reader = getReader();
		//this.dataFromPipe.clear();
		byte[] data = {};
		while(data.length==0){ //TODO: Nathan fix me 
			while (PipeReader.tryReadFragment(readPipe)) {		
				System.out.println("I'm stuck in readData()"); 
				assert(PipeReader.isNewMessage(readPipe)) : "This test should only have one simple message made up of one fragment";
				int msgIdx = PipeReader.getMsgIdx(readPipe);

				if(RawDataSchema.MSG_CHUNKEDSTREAM_1 == msgIdx){
					reader.openHighLevelAPIField(RawDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
					try {
						data = new byte[reader.readByte()]; //TODO: Nathan take out the trash
						for (int i = 0; i < data.length; i++) {
							data[i]=reader.readByte(); 
						}
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
				//dataFromPipe.add(data);

				PipeReader.releaseReadLock(readPipe);
			} 
		}
		System.out.println("I've escaped!");
		return data; 
	}


	private DataOutputBlobWriter<RawDataSchema> getWriter(){
		//assert Pipe.isInit(toHardware);
		if(null == writer){
			this.writer = new DataOutputBlobWriter<RawDataSchema>(writePipe);
		}
		return writer;
	}
	private DataInputBlobReader<RawDataSchema> getReader(){
		if(null == reader){
			this.reader = new DataInputBlobReader<RawDataSchema>(readPipe);
		}
		return reader;
	}
}
