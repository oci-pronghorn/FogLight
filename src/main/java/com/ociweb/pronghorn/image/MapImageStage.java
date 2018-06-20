package com.ociweb.pronghorn.image;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.pipe.ChannelWriter;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.pipe.RawDataSchemaUtil;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.math.HistogramSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.primitive.Lois;
import com.ociweb.pronghorn.util.primitive.LoisVisitor;

public class MapImageStage extends PronghornStage {

	private int[]   workspace;
	private Lois locations; 	
	private int [] imageLookup;
	private int imageWidth;
	private int imageHeight;
	private int imageDepth = 256;//default value
		
	private transient int loadPosition = -1;
	private transient int savePosition = -1;
	
	private final Pipe<ImageSchema> imgInput; 
    private final Pipe<RawDataSchema> loadingMappingData;
    private final Pipe<RawDataSchema> savingMappingData; 
    private final Pipe<LocationModeSchema> modeIn;
    private final Pipe<CalibrationStatusSchema> statusOut;
	private final Pipe<HistogramSchema> output;
	
	private boolean isShuttingDown = false;
	
	private boolean loadingNewMap = false;	
	private boolean imageInProgress = false;
	
	private int totalRows;
	private int totalWidth;
	private long time;
	private int activeRow;
	
	private LoisVisitor sumVisitor = new LoisVisitor() {
		@Override
		public boolean visit(int value) {
			workspace[value]++;
			return true;
		}
		
	};

	
	//TODO: these are not yet done....
	private boolean isLearning = false; //from mode
	private int activeLearningLocationBase = 10000000; //from mode
	private int learningMaxSlices = 100000; //fixed,from mode control
	private int cycleStep = 0; //member... clear on mode							



	//TODO: ad traning of loop
	//TODO: must pass in uppper range so we can detect this when encountered.
	
	
	
	
	private static final Logger logger = LoggerFactory.getLogger(MapImageStage.class);
    	
	public static MapImageStage newInstance(GraphManager graphManager, 
            Pipe<ImageSchema> imgInput, 
            Pipe<LocationModeSchema> stateData,
            Pipe<HistogramSchema> output,
            Pipe<CalibrationStatusSchema> done,
            Pipe<RawDataSchema> loadingMappingData,
            Pipe<RawDataSchema> savingMappingData            
            ) {
		return new MapImageStage(graphManager, imgInput, stateData, output, done, loadingMappingData, savingMappingData);
	}
	
	//need outgoing schema for the map.
	protected MapImageStage(GraphManager graphManager, 
			                Pipe<ImageSchema> imgInput, 
			                Pipe<LocationModeSchema> modeIn,
			                Pipe<HistogramSchema> output,
			                Pipe<CalibrationStatusSchema> statusOut,
			                Pipe<RawDataSchema> loadingMappingData,
			                Pipe<RawDataSchema> savingMappingData 
			               ) {
		
		super(graphManager, join(imgInput,loadingMappingData, modeIn), join(output,savingMappingData, statusOut) );
		
		this.imgInput = imgInput;
		this.loadingMappingData = loadingMappingData;
		this.savingMappingData = savingMappingData;
		this.modeIn = modeIn;
		this.output = output;
		this.statusOut = statusOut;
	}

	
	
	@Override
	public void run() {
		
		assert(savePosition!=-2 || loadPosition!=-2) : "Can only load or save but not do both at same time.";
		
		//NOTE: if we are still saving the data do this first
		if (savePosition == -2) {
			if (locations.save(savingMappingData)) {
				savePosition = -3;//done				
			} else {
				return;
			}
		}
		
		//NOTE: if we are still loading the data do this first.
		if (loadPosition == -2) {
			if (locations.load(loadingMappingData)) {
				loadPosition = -1;//done					
			} else {
				//need to try load again later
				return;
			}
		}
		
		if (!isShuttingDown) {
			if (!imageInProgress) {
				if (Pipe.hasContentToRead(loadingMappingData)) {
					if (!load(loadingMappingData)) {
						return;
					}
				}	
				
				if (Pipe.hasContentToRead(modeIn)) {
					readModeData(modeIn);	
				}
				
			} else {
				//we are image in progress
				if (activeRow == totalRows) {
					//we have a complete message to send
					publishHistogram();
					
					finishedImageProcessing();
				}				
			}
			
			//if we are not loading a new map check for an image to process
			if (!loadingNewMap) {
				
				
			    //must have room to write results if we read any data.
				while (Pipe.hasContentToRead(imgInput) 
						&& Pipe.hasRoomForWrite(output)
						&& Pipe.hasRoomForWrite(statusOut)) {
					
					int msgIdx = Pipe.takeMsgIdx(imgInput);
					
					if (ImageSchema.MSG_FRAMECHUNK_2 == msgIdx) {
						if (activeRow < totalRows) {
			
							
							DataInputBlobReader<ImageSchema> rowData = Pipe.openInputStream(imgInput);							
							int rowBase = (imageWidth*imageDepth)*activeRow++;

							
							if (!isLearning) {
								///////////////////////
								//normal location scanning
								///////////////////////
								for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {
									locations.visitSet(locationSetId(rowData, rowBase, activeColumn), sumVisitor );
								}
								if (activeRow == totalRows) {
									publishHistogram();									
									finishedImageProcessing();
								}						
							} else {
								/////////////////////
								//learning
								/////////////////////
								
								//given this root have we already seen this position recorded
								//if so we are done, sent back done status								
								if (isCycleComplete(rowData, rowBase, activeLearningLocationBase, learningMaxSlices)) {
									//send done status to see if the other actors agree									
									publishCycleDone(activeLearningLocationBase, cycleStep);
								} 
								
								//generate new location id
								int activeLocation = activeLearningLocationBase + cycleStep++;
								//learn this new location
								for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {
									
									locations.insert(locationSetId(rowData, rowBase, activeColumn), 
													 activeLocation);
								}
								if (activeRow == totalRows) {
									//no histogram to send..
									finishedImageProcessing();
								}
								
							}
							
							
							
						} else {
							//error too many rows.
							logger.error("too many rows only expected {}",totalRows);
							Pipe.skipNextFragment(imgInput, msgIdx);
						}
					} else if (ImageSchema.MSG_FRAMESTART_1 == msgIdx) {
						
						imageInProgress = true;
						totalWidth = Pipe.takeInt(imgInput);
						totalRows = Pipe.takeInt(imgInput);
						time = Pipe.takeLong(imgInput); 
						//clear histogram totals
						Arrays.fill(workspace, 0);
						activeRow = 0;
						
					} else {
						
						isShuttingDown = true;
						Pipe.confirmLowLevelWrite(imgInput, Pipe.EOF_SIZE);
						Pipe.releaseReadLock(imgInput);
						break;
					}
				}
			}		
		} else {			
			if (savePosition==-3 || save(savingMappingData)) {
				if (Pipe.hasRoomForWrite(output)) {
					Pipe.publishEOF(output);
					requestShutdown();
				}
			}
		}
	}

	private void readModeData(Pipe<LocationModeSchema> pipe) {
		
		while (Pipe.hasContentToRead(pipe)) {
			
			int msgIdx = Pipe.takeMsgIdx(pipe);
			
			
//			public static final int MSG_CYCLELEARNINGSTART_1 = 0x00000000; //Group/OpenTempl/3
//			public static final int MSG_CYCLELEARNINGSTART_1_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
//			public static final int MSG_CYCLELEARNINGSTART_1_FIELD_MAXSTEPS_13 = 0x00000002; //IntegerUnsigned/None/1
//			public static final int MSG_CYCLELEARNINGCOMPLETED_2 = 0x00000004; //Group/OpenTempl/3
//			public static final int MSG_CYCLELEARNINGCOMPLETED_2_FIELD_STARTVALUE_12 = 0x00000001; //IntegerUnsigned/None/0
//			public static final int MSG_CYCLELEARNINGCOMPLETED_2_FIELD_TOTALSTEPS_23 = 0x00000002; //IntegerUnsigned/None/2
//			public static final int MSG_CYCLELEARNINGCANCEL_3 = 0x00000008; //Group/OpenTempl/1
			
			
			
		}
		
		
		//read the start learning... (sent by request to start learning, clears on its own)
		///    base value
		///    max steps
		
		
		//stop learning as of position x  (sent by consensus and cyclic barier sent to listener..) 
		//     remove following y locations
		
		
		//remove tick...
		
		
		// TODO do we need one of these per every image?
		
		
		
	}

	private void publishCycleDone(int activeLearningLocationBase, int cycleStep) {
		
		Pipe.presumeRoomForWrite(statusOut);
		int size = Pipe.addMsgIdx(statusOut, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1);
		Pipe.addIntValue(activeLearningLocationBase, statusOut);
		Pipe.addIntValue(cycleStep, statusOut);
		Pipe.confirmLowLevelWrite(statusOut, size);
		Pipe.publishWrites(statusOut);
		
	}

	private boolean isCycleComplete(DataInputBlobReader<ImageSchema> rowData, int rowBase,
			int activeLearningLocationBase, int learningMaxSlices) {
		boolean isLoopCompleted = false;
		int endValue = activeLearningLocationBase+learningMaxSlices;
		int totalMatches = 0;
		int countLimit = (totalWidth*4)/3;
		for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {								
			if (locations.containsAny(locationSetId(rowData, rowBase, activeColumn),
					                  activeLearningLocationBase, endValue)) {
				if (isLoopCompleted=(++totalMatches>countLimit)) {											
					break;
				}
				
			};									
		}
		return isLoopCompleted;
	}

	private int locationSetId(DataInputBlobReader<ImageSchema> rowData, int rowBase, int activeColumn) {
		return imageLookup[
		                               rowBase                            
		                               +(activeColumn*imageDepth)
		                               +(int)rowData.readByte()];
	}


	public boolean save(Pipe<RawDataSchema> pipe) {
		assert (pipe.maxVarLen<(ChannelReader.PACKED_INT_SIZE*4)) : "Pipes must hold longer messages to write this content";
				
		while (Pipe.hasRoomForWrite(pipe)) {					
			int size = Pipe.addMsgIdx(pipe, RawDataSchema.MSG_CHUNKEDSTREAM_1);
			ChannelWriter writer = Pipe.openOutputStream(pipe);
			if (savePosition==-1) { //new file
				
				writer.writePackedInt(imageWidth);
				writer.writePackedInt(imageHeight);
				writer.writePackedInt(imageDepth);
				writer.writePackedInt(workspace.length); //locations
		
				savePosition = 0;
			}
			while (savePosition<imageLookup.length && writer.remaining()>=ChannelReader.PACKED_INT_SIZE) {
				writer.writePackedInt(imageLookup[savePosition++]);				
			}			
			writer.closeLowLevelField();
			Pipe.confirmLowLevelWrite(pipe, size);
			Pipe.publishWrites(pipe);
		
			if (savePosition==imageLookup.length) {
				savePosition = -2;
				
				boolean result = locations.save(pipe); //if in this state keep calling.
				if (result) {
					loadPosition = -1;//done					
				}
				
				return result;
			}
		}
		return false;
	}
	
	private boolean load(Pipe<RawDataSchema> pipe) {
		
		while (Pipe.hasContentToRead(pipe)) {
			boolean isEnd = RawDataSchemaUtil.accumulateInputStream(pipe);
			ChannelReader reader = Pipe.inputStream(pipe);			
			int startingAvailable = reader.available();
			
			if (loadPosition == -1) {
				
				//note this value here forces us to keep init at 16 and min block at 4
				if (reader.available() < (ChannelReader.PACKED_INT_SIZE*4)) {
					return false;//not enough data yet to read header cleanly
				}
			
				//load all the fixed constants here
				int width  	  = reader.readPackedInt();
				int height 	  = reader.readPackedInt();
				int depth  	  = reader.readPackedInt();
				int locations = reader.readPackedInt();//max location value+1
				
				if (null == workspace || workspace.length != locations) {
					workspace = new int[locations];
				}
				imageWidth = width;
				imageHeight = height;
				imageDepth = depth;
				final int imageLookupLength = width*height*depth;
				
				//init the image matrix as needed		
				if (null ==	imageLookup || imageLookup.length != imageLookupLength) {
					imageLookup = new int[imageLookupLength];
				}
				loadPosition = 0;
			}

			while ( ((reader.available() >= ChannelReader.PACKED_INT_SIZE) || isEnd) 
					&& loadPosition<imageLookup.length ) {
				imageLookup[loadPosition++] = reader.readPackedInt();
			}
						
			RawDataSchemaUtil.releaseConsumed(pipe, reader, startingAvailable);
			
			if (loadPosition == imageLookup.length) {
				loadPosition = -2;
				boolean result = locations.load(pipe); //if in this state keep calling.
				if (result) {
					loadPosition = -1;//done					
				}
				return result;
			}
		}
		return false;
	}

	private void publishHistogram() {
		Pipe.presumeRoomForWrite(output);

		int size = Pipe.addMsgIdx(output, HistogramSchema.MSG_HISTOGRAM_1);
		
		Pipe.addIntValue(workspace.length, output);
		
		DataOutputBlobWriter<HistogramSchema> outputStream = Pipe.openOutputStream(output);								
		int i = workspace.length;
		while (--i>=0) {
			outputStream.writePackedInt(workspace[i]);
		}
		DataOutputBlobWriter.closeLowLevelField(outputStream);
		
		Pipe.confirmLowLevelWrite(output, size);
		Pipe.releaseReadLock(output);
	}

	private void finishedImageProcessing() {
		totalRows = 0;//clear we have sent the value.
		imageInProgress = false;
	}
	

}
