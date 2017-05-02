package com.ociweb.iot.valveManifold;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class ValveDataParserStage extends PronghornStage {
	
	private final static Logger logger = LoggerFactory.getLogger(ValveDataParserStage.class);
	
	public static final int DATA_START = 						1;
	public static final int DATA_END =   						2;
	public static final int DATA_IGNORE =   					3;
	
	
	private TrieParser trie;
	private TrieParserReader reader;
	
	private Pipe<RawDataSchema> input;
	private Pipe<ValveSchema> output;
	private boolean EOF_Detected = false;
	private int stationNumber;
	private boolean isNew = true;
	
	public static void newInstance(GraphManager gm, Pipe<RawDataSchema> input, Pipe<ValveSchema> output) {
		new ValveDataParserStage(gm, input, output);		
	}
	
	protected ValveDataParserStage(GraphManager graphManager,  Pipe<RawDataSchema> input, Pipe<ValveSchema> output) {
		super(graphManager, input, output);
		this.input = input;
		this.output = output;
	}

	
	@Override
	public void startup() {
		trie = buildParser();
		reader = new TrieParserReader(1);
		//logger.info("started up");
	}
	
	@Override
	public void shutdown() {
		//logger.info("shutting down");
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
		//logger.info("called run");
		while ((TrieParserReader.parseHasContent(reader)||Pipe.hasContentToRead(input)||EOF_Detected) && Pipe.hasRoomForWrite(output)) {
		
			//logger.info("has content to read");
			
			readDataIntoParser(); //load all we can into the parser			
			if (!publishAvailData()) { //parse everything we can 
				return;//come back later, could not parse what we have so far.
			}
			if ( (!TrieParserReader.parseHasContent(reader)) && EOF_Detected) {
				requestShutdown();
				return;
				
			}
		}

	}

	private boolean publishAvailData() {
				
		if (isNew) {
			//nothing happens until we find '['
			if (TrieParserReader.parseSkipUntil(reader,'[')) {
				isNew=false;	
				//continue on to parse
			} else {
				return false; //the beginning of a data pack was not found, try again later.
			}
		}
				
		int originalLen = reader.sourceLen;
		while (Pipe.hasRoomForWrite(output)) {
		
			int oldPos = reader.sourcePos;
			int parsedId = (int)TrieParserReader.parseNext(reader, trie);
			
		    if (parsedId>0) {
		    	//valid values

		    	if (DATA_IGNORE!=parsedId) {
					if (DATA_START==parsedId) {
						stationNumber = (int)TrieParserReader.capturedLongField(reader, 0);						
					} else if (DATA_END==parsedId){				
						//clear the st so it is not used for unexpected content
						stationNumber = -1;
					} else {
						if (stationNumber != -1) {						
							publishSingleMessage(parsedId);						
						}					
					}
		    	}
			    	
		    	
		    } else {
		    	
		    	assert (reader.sourcePos==oldPos);
		    	
		    	int length = originalLen-reader.sourceLen;
				Pipe.releasePendingAsReadLock(input, length);
		    	//exit the while we need read more content
		    	return false;
		    }
		}
		//release all the bytes we ahve consumed in the above loop.
		int length = originalLen-reader.sourceLen;
		Pipe.releasePendingAsReadLock(input, length);

		return true;
		
	}

	private void publishSingleMessage(int parsedId) {
		final int size = Pipe.addMsgIdx(output, parsedId);
		Pipe.addIntValue(stationNumber,output);
					
		//logger.info("publish message stationNo:{} type:{} ",stationNumber, parsedId);
		
		switch (parsedId) {

			case ValveSchema.MSG_PARTNUMBER_330:
				
				DataOutputBlobWriter<ValveSchema> writer = Pipe.outputStream(output);
				writer.openField();
				TrieParserReader.capturedFieldBytesAsUTF8(reader, 0, writer);
				writer.closeLowLevelField();

				break;						
			case ValveSchema.MSG_VALUEFAULT_FALSE_340:	
			case ValveSchema.MSG_VALUEFAULT_TRUE_341:
			case ValveSchema.MSG_LEAKFAULT_FALSE_360:
			case ValveSchema.MSG_LEAKFAULT_TRUE_361:
			case ValveSchema.MSG_PRESSUREFAULT_LOW_350:
			case ValveSchema.MSG_PRESSUREFAULT_NONE_351:
			case ValveSchema.MSG_PRESSUREFAULT_HIGH_352:
				//NOTE: nothing to do here we have no payload
				break;						
			default:

				//simple value
				Pipe.addIntValue((int)TrieParserReader.capturedLongField(reader, 0), output);
				
		}	
		
		Pipe.confirmLowLevelWrite(output, size);
		Pipe.publishWrites(output);
		
	}

	private void readDataIntoParser() {
		while (Pipe.hasContentToRead(input)) {
			int msgIdx = Pipe.takeMsgIdx(input);
			if (RawDataSchema.MSG_CHUNKEDSTREAM_1==msgIdx) {
				
				if (reader.sourceLen>0) {
					//we have old data so just add the new data to it.						
					int meta = Pipe.takeRingByteMetaData(input);
					int length    = Pipe.takeRingByteLen(input);
					int pos = Pipe.bytePosition(meta, input, length); //moves byte counter forward.
					
					reader.sourceLen+=length;				
					
				} else {
					TrieParserReader.parseSetup(reader, input);
				}
				
				Pipe.confirmLowLevelRead(input, Pipe.sizeOf(RawDataSchema.instance, RawDataSchema.MSG_CHUNKEDSTREAM_1)); 
				Pipe.readNextWithoutReleasingReadLock(input);
									
				
			} else {
				
				assert(-1 == msgIdx);
				Pipe.confirmLowLevelRead(input, Pipe.EOF_SIZE);
				Pipe.releaseReadLock(input);
				EOF_Detected = true;
				break;
				
			}
		}
	}

	
//	Manifold serial number: u32
//	Station number: u8
//	Valve serial number: u32
//	Part number: string
//	Life cycle count: u32
//	Valve fault: u8
//	Pressure fault: 1-character string
//	Leak fault: u8
//	Supply pressure: u16
//	Duration of last 1-4 signal: u32
//	Duration of last 1-2 signal: u32
//
//	Equalization average pressure: u16
//	Equalization pressure rate: i16
//	Residual of dynamic analysis: u16

	
	
    //package protected for testing
	static TrieParser buildParser() {
		
		TrieParser tp = new TrieParser(256,1,true,true);
		
		/////////////////////////
		//these are needed to "capture" error cases early, the tighter we make them the sooner we find the issues.
		tp.setMaxNumericLengthCapturable(16);
		tp.setMaxBytesCapturable(36);
        /////////////////////////
		
		tp.setUTF8Value("[st%u",        DATA_START);
		
		tp.setUTF8Value("mn%i",         ValveSchema.MSG_MANIFOLDSERIALNUMBER_310);
		tp.setUTF8Value("sn%i",        	ValveSchema.MSG_VALVESERIALNUMBER_311);
		tp.setUTF8Value("pn\"%b\"",    	ValveSchema.MSG_PARTNUMBER_330);
		tp.setUTF8Value("cc%i",     	ValveSchema.MSG_LIFECYCLECOUNT_312);
		
		tp.setUTF8Value("sp%i",     	ValveSchema.MSG_SUPPLYPRESSURE_313);	
		
		tp.setUTF8Value("pp%i",     	ValveSchema.MSG_PRESSUREPOINT_319);		
		
		tp.setUTF8Value("da%i",     	ValveSchema.MSG_DURATIONOFLAST1_4SIGNAL_314);		
		tp.setUTF8Value("db%i",     	ValveSchema.MSG_DURATIONOFLAST1_2SIGNAL_315);		
		tp.setUTF8Value("ap%i",     	ValveSchema.MSG_EQUALIZATIONAVERAGEPRESSURE_316);		
		tp.setUTF8Value("ep%i",     	ValveSchema.MSG_EQUALIZATIONPRESSURERATE_317);		
		tp.setUTF8Value("lr%i",     	ValveSchema.MSG_RESIDUALOFDYNAMICANALYSIS_318);		
					
		tp.setUTF8Value("vf0",     		ValveSchema.MSG_VALUEFAULT_FALSE_340);
		tp.setUTF8Value("vf1",     		ValveSchema.MSG_VALUEFAULT_TRUE_341);
			
		
		tp.setUTF8Value("pf\"L\"", 		ValveSchema.MSG_PRESSUREFAULT_LOW_350);
		tp.setUTF8Value("pf\"N\"", 		ValveSchema.MSG_PRESSUREFAULT_NONE_351);
		tp.setUTF8Value("pf\"H\"", 		ValveSchema.MSG_PRESSUREFAULT_HIGH_352);
				
		
		tp.setUTF8Value("lf0",     		ValveSchema.MSG_LEAKFAULT_FALSE_360);
		tp.setUTF8Value("lf1",     	   	ValveSchema.MSG_LEAKFAULT_TRUE_361);
				
		
		tp.setUTF8Value("]",         	DATA_END);
		
		tp.setUTF8Value("\n",      	    DATA_IGNORE);
		tp.setUTF8Value("\r",      	    DATA_IGNORE);
		
				
		return tp;
		
	}
	
}
