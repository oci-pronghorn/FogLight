package com.ociweb.iot.hardware;

import static com.ociweb.pronghorn.pipe.PipeWriter.publishWrites;
import static com.ociweb.pronghorn.pipe.PipeWriter.tryWriteFragment;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.iot.hardware.IODevice;
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
import com.ociweb.pronghorn.util.Blocker;
import com.ociweb.pronghorn.iot.schema.AcknowledgeSchema;
import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class AnalogDigitalOutputStage extends PronghornStage {
	
//	private final Pipe<GroveRequestSchema> []fromCommandChannel;
	private final Pipe<GroveRequestSchema> fromCommandChannel;
	private final Pipe<GoSchema> goPipe;
	private final Pipe<AcknowledgeSchema> ackPipe;
	private DataOutputBlobWriter<AcknowledgeSchema> writeAck;
	private DataInputBlobReader<GroveRequestSchema> readCommandChannel;
	private DataInputBlobReader<GoSchema> readGo;

	private Hardware hardware;
	private int goCount;
	private int ackCount;
	private int connector;//should be passed in first
	private int value;
	private int duration;
	
	private Blocker blocker = new Blocker(16);// max of 16 pipes can be waiting with different times.
	private static final short activeBits = 4; //we have a max of 16 physical ports to use on the groveShield
    private static final short activeSize = (short)(1<<activeBits);
    private int[][]    movingAverageHistory;
    private int[]      lastPublished;

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
			this.readCommandChannel = new DataInputBlobReader<GroveRequestSchema>(fromCommandChannel);
			this.connector = 0;
			this.goCount = 0;
			this.ackCount= 0;
			this.value = 0;
//			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//			 int j = hardware.maxAnalogMovingAverage()-1;
//		        movingAverageHistory = new int[j][]; 
//		        while (--j>=0) {
//		            movingAverageHistory[j] = new int[activeSize];            
//		        }
//		        lastPublished = new int[activeSize];
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		// need to change to make the Edison PIN to startup correctly
		super.startup();
		//call the super.startup() last to keep schedulers from getting too eager and starting early
	}

	@Override
	public void run() { //message: {address, package size, bytes to be read, package[]}
		goCount =0;
		long now = System.currentTimeMillis();
//        int j = fromCommandChannel.length;
//        while (--j>=0) {
//            processPipe(fromCommandChannel[j],now);
//            
//        }
		
		while (PipeReader.tryReadFragment(goPipe)) {		
			assert(PipeReader.isNewMessage(goPipe)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(goPipe);
			
			if(GoSchema.MSG_RELEASE_20== msgIdx){
				goCount += PipeReader.readInt(goPipe, GoSchema.MSG_RELEASE_20_FIELD_COUNT_22);
				ackCount = goCount;
				System.out.println("Received Go Command "+goCount);
			}else{
				assert(msgIdx == -1);
				requestShutdown();
			}
			PipeReader.releaseReadLock(goPipe);
		} 
	while (goCount > 0 && PipeReader.tryReadFragment(fromCommandChannel)){//might need to add: &&!blocker.isBlocked(Pipe.peekInt(fromCommandChannel, 1))
			assert(PipeReader.isNewMessage(fromCommandChannel)) : "This test should only have one simple message made up of one fragment";
			int msgIdx = PipeReader.getMsgIdx(fromCommandChannel);
			switch(msgIdx){
			
			case GroveRequestSchema.MSG_DIGITALSET_110:
			{
				connector = PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111);
				if (blocker.isBlocked(connector)) {
                    throw new UnsupportedOperationException();
                }
				value = 	PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112); 
				hardware.digitalWrite(connector, value);
				System.out.println("digitalWrite sent to Ed PinManager");
				break;
			}
		 
			case GroveRequestSchema.MSG_BLOCK_220:
            {
            	connector = PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_BLOCK_220_FIELD_CONNECTOR_111);
            	if (blocker.isBlocked(connector)) {
                    throw new UnsupportedOperationException();
                }
            	duration = 	PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_BLOCK_220_FIELD_DURATION_113); 
            	blocker.until(connector, now + (long)duration);
            	 Pipe.confirmLowLevelRead(fromCommandChannel, Pipe.sizeOf(fromCommandChannel, msgIdx));
            	 PipeReader.releaseReadLock(fromCommandChannel);
				//hardware.analogWrite(connector, value);
				System.out.println("analogWrite sent to Ed PinManager");
				break;//return;//TODO: it would be nice to remove this return
			}
            //break;
			case GroveRequestSchema.MSG_ANALOGSET_140:
            { 
            	connector = PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141);
                if (blocker.isBlocked(connector)) {
                    throw new UnsupportedOperationException();
                }
                value =		PipeReader.readInt(fromCommandChannel,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142);
                hardware.analogWrite(connector, value);
                break; 
            }   
          
			default:
			{
				System.out.println("Wrong Message index "+msgIdx);
				assert(msgIdx == -1);
				requestShutdown();
			}
	}
			 Pipe.confirmLowLevelRead(fromCommandChannel, Pipe.sizeOf(fromCommandChannel, msgIdx));
			 PipeReader.releaseReadLock(fromCommandChannel);
				
			 if(goCount==0){
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