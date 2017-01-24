package com.ociweb.iot.valveManifold;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class ValveDataParserStage extends PronghornStage {
	
	public static final int DATA_START = 						1;
	public static final int DATA_END =   						2;
	
	private TrieParser trie;
	private TrieParserReader reader;
	
	private Pipe<RawDataSchema> input;
	private Pipe<ValveSchema> output;
	private boolean EOF_Detected = false;
	private int stationNumber;
	
	public static void instance(GraphManager gm, Pipe<RawDataSchema> input, Pipe<ValveSchema> output) {
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
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
		while ((Pipe.hasContentToRead(input)||EOF_Detected) && Pipe.hasRoomForWrite(output)) {
		
			readDataIntoParser(); //load all we can into the parser			
			publishAvailData(); //parse everything we can 
			
			if ( (!TrieParserReader.parseHasContent(reader)) && EOF_Detected) {
				requestShutdown();
				return;
				
			}
		}
		
	}

	private void publishAvailData() {
		
		int originalLen = reader.sourceLen;
		while (Pipe.hasRoomForWrite(output)) {
			
			int parsedId = (int)TrieParserReader.parseNext(reader, trie);
		    if (parsedId>0) {
		    	//valid values
			     
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
			    	
		    } else {
		    	break;//exit the while we need read more content
		    }
		}
		//release all the bytes we ahve consumed in the above loop.
		Pipe.releasePendingAsReadLock(output, originalLen-reader.sourceLen);
		
	}

	private void publishSingleMessage(int parsedId) {
		final int size = Pipe.addMsgIdx(output, parsedId);
		Pipe.addIntValue(stationNumber,output);
							
		switch (parsedId) {

			case ValveSchema.MSG_PARTNUMBERMESSAGE_330:
				
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
					Pipe.bytePosition(meta, input, length); //moves byte counter forward.
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

	
    //package protected for testing
	static TrieParser buildParser() {
		
		TrieParser tp = new TrieParser(256,1,true,true);

		tp.setUTF8Value("[st%u",        DATA_START);
		
		tp.setUTF8Value("mn%i",         ValveSchema.MSG_MANIFOLDSERIALNUMBERMESSAGE_310);
		tp.setUTF8Value("sn%i",        	ValveSchema.MSG_VALVESERIALNUMBERMESSAGE_311);
		tp.setUTF8Value("pn\"%b\"",    	ValveSchema.MSG_PARTNUMBERMESSAGE_330);
		tp.setUTF8Value("cc%i",     	ValveSchema.MSG_LIFECYCLECOUNTMESSAGE_312);
		tp.setUTF8Value("sp%i",     	ValveSchema.MSG_SUPPLYPRESSUREMESSAGE_313);		
		tp.setUTF8Value("da%i",     	ValveSchema.MSG_DURATIONOFLAST1_4SIGNALMESSAGE_314);		
		tp.setUTF8Value("db%i",     	ValveSchema.MSG_DURATIONOFLAST1_2SIGNALMESSAGE_315);		
		tp.setUTF8Value("ap%i",     	ValveSchema.MSG_EQUALIZATIONAVERAGEPRESSUREMESSAGE_316);		
		tp.setUTF8Value("ep%i",     	ValveSchema.MSG_EQUALIZATIONPRESSURERATEMESSAGE_317);		
		tp.setUTF8Value("lr%i",     	ValveSchema.MSG_RESIDUALOFDYNAMICANALYSISMESSAGE_318);		
					
		tp.setUTF8Value("vf0",     		ValveSchema.MSG_VALUEFAULT_FALSE_340);
		tp.setUTF8Value("vf1",     		ValveSchema.MSG_VALUEFAULT_TRUE_341);
			
		
		tp.setUTF8Value("pf\"L\"", 		ValveSchema.MSG_PRESSUREFAULT_LOW_350);
		tp.setUTF8Value("pf\"N\"", 		ValveSchema.MSG_PRESSUREFAULT_NONE_351);
		tp.setUTF8Value("pf\"H\"", 		ValveSchema.MSG_PRESSUREFAULT_HIGH_352);
				
		
		tp.setUTF8Value("lf0",     		ValveSchema.MSG_LEAKFAULT_FALSE_360);
		tp.setUTF8Value("lf1",     	   	ValveSchema.MSG_LEAKFAULT_TRUE_361);
				
		
		tp.setUTF8Value("]",         	DATA_END);
				
		return tp;
		
	}
	
}
