package com.ociweb.pronghorn.image;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.image.schema.CalibrationStatusSchema;
import com.ociweb.pronghorn.image.schema.LocationModeSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
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

	private static final int NO_DATA = -1;
	private static final int SINGLE_BASE = -2;
	
	private int[]   workspace;
	private Lois locations; 	
	private int [] imageLookup;
	private int imageWidth;
	private int imageHeight;

		
	private transient int loadPosition = NO_DATA;
	private transient int savePosition = NO_DATA;
	
	private final Pipe<ImageSchema> imgInput; 
    private final Pipe<RawDataSchema> loadingMappingData;
    private final Pipe<RawDataSchema> savingMappingData; 
    private final Pipe<LocationModeSchema> modeIn;
    private final Pipe<CalibrationStatusSchema> statusOut;
    private final Pipe<CalibrationStatusSchema> ack;
    
	private final Pipe<HistogramSchema> output;
	
	private boolean isShuttingDown = false;	
	private boolean loadingNewMap = false;	
	private boolean imageInProgress = false;
	
	private int totalRows;
	private int totalWidth;
	private long time;
	private int activeRow;
	
	//this provides for 64 colors which both helps with
	//   * simplification of what needs to be seen
	//   * significant reduction in memory consumption
	private int shiftColors = 2;
	private int localDepth = 256 >> shiftColors;
	//private int minCycles = 12;
	
	private LoisVisitor sumVisitor = new LoisVisitor() {
		@Override
		public boolean visit(int value) {
			workspace[value]++;
			return true;
		}
		
	};

	private boolean hasDataSet = false;
	private boolean isLearning = false; 
	private int activeLearningLocationBase;
	private int learningMaxSlices; //cycle may use less but not more than this or the location will change
	private int cycleStep;//total steps in this cycle

	private static final Logger logger = LoggerFactory.getLogger(MapImageStage.class);
    	
	public static MapImageStage newInstance(GraphManager graphManager, 
            Pipe<ImageSchema> imgInput, 
            Pipe<LocationModeSchema> stateData,
            Pipe<HistogramSchema> output,
            Pipe<CalibrationStatusSchema> ack, 
            Pipe<CalibrationStatusSchema> done,
            Pipe<RawDataSchema> loadingMappingData,
            Pipe<RawDataSchema> savingMappingData,
			String colorLabel
            ) {
		return new MapImageStage(graphManager, imgInput, stateData, output, ack, done, loadingMappingData, savingMappingData, colorLabel);
	}
	
	//need outgoing schema for the map.
	protected MapImageStage(GraphManager graphManager, 
			                Pipe<ImageSchema> imgInput, 
			                Pipe<LocationModeSchema> modeIn,
			                Pipe<HistogramSchema> output,
			                Pipe<CalibrationStatusSchema> ack,
			                Pipe<CalibrationStatusSchema> statusOut,
			                Pipe<RawDataSchema> loadingMappingData,
			                Pipe<RawDataSchema> savingMappingData,
							String colorLabel
			               ) {
		
		super(graphManager, join(imgInput,loadingMappingData, modeIn, ack), join(output, savingMappingData, statusOut) );
		
		this.imgInput = imgInput;
		this.loadingMappingData = loadingMappingData;
		this.savingMappingData = savingMappingData;
		this.modeIn = modeIn;
		this.output = output;
		this.statusOut = statusOut;
		this.ack = ack;
		
		GraphManager.addNota(graphManager, GraphManager.DOT_RANK_NAME, "ModuleStage", this);
		GraphManager.addNota(graphManager, GraphManager.STAGE_NAME, colorLabel, this);

	}

	@Override
	public void startup() {
		locations = new Lois();
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
				loadPosition = NO_DATA;//done					
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

				if (Pipe.hasContentToRead(ack)) {
					readAckData(ack);	
				}
				
				if (Pipe.hasContentToRead(modeIn)) {
					readModeData(modeIn);	
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
							int rowBase = (imageWidth*localDepth)*activeRow++;

							if (!isLearning) {
								///////////////////////
								//normal location scanning
								///////////////////////
								for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {
									int readByte = (0xFF&rowData.readByte())>>shiftColors;
									
									int locationSetId = getLocationSetId(rowBase, activeColumn, readByte);
									if (NO_DATA != locationSetId) {
										if (locationSetId<0) {										
											//we have a single value so convert and match it
											sumVisitor.visit(SINGLE_BASE - locationSetId);											
										} else {
											locations.visitSet(locationSetId, sumVisitor );
										}
									}
								}
								if (activeRow == totalRows) {
									
									if (hasDataSet) {		
										//only publish if is valid									
										publishHistogram();	
									}
									
									finishedImageProcessing();
								}						
							} else {
								/////////////////////
								//learning
								/////////////////////
								
								//given this root have we already seen this position recorded
								//if so we are done, sent back done status	
								final int dataPos = rowData.absolutePosition();
								if (/*cycleStep>minCycles &&*/ isCycleComplete(rowData, rowBase, activeLearningLocationBase)) {
									
									hasDataSet = true;
									//send done status to see if the other actors agree									
									publishCycleDone(activeLearningLocationBase, cycleStep);
									
								} 
								
								//generate new location id
								int activeLocation = activeLearningLocationBase + cycleStep;
											
								//restore beginning of data to read again
								rowData.absolutePosition(dataPos);								
								//learn this new location
								for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {

									int readByte = (0xFF&rowData.readByte())>>shiftColors;
									
									int locationSetId = getLocationSetId(rowBase, activeColumn, readByte);
									if (NO_DATA != locationSetId) {
										if (locationSetId<0) {
											//we now have 2 values stored here so extract first and collect both
											int firstValue = SINGLE_BASE-locationSetId;
											
											locationSetId = locations.newSet();
											locations.insert(locationSetId, firstValue);
					
											setLocationSetId(rowBase, activeColumn, locationSetId, readByte);
										}
										locations.insert(locationSetId, activeLocation);
										
									} else {
										//store single value as negative until a second needs to be stored
						
										setLocationSetId(rowBase, activeColumn, SINGLE_BASE-activeLocation, readByte);
									}
									
								}
								if (activeRow == totalRows) {
									//ensure steps stays under the max slice value so location base is not disturbed.
									if (++cycleStep >= learningMaxSlices) {
										logger.warn("the cycle step was too large {} and over {}", cycleStep, learningMaxSlices);
										cycleStep = 0; 
									}
									//no histogram to send..
									finishedImageProcessing();
								}
								
							}
							Pipe.confirmLowLevelRead(imgInput, Pipe.sizeOf(imgInput, msgIdx));
							Pipe.releaseReadLock(imgInput);	
							
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
						
						int frameBytes = Pipe.takeInt(imgInput);
						int bitsPerPixel =  Pipe.takeInt(imgInput);
						ChannelReader reader = Pipe.openInputStream(imgInput);
						
						if (null == workspace || imageWidth!=totalWidth || imageHeight!=totalRows) {
			
							int maxLocatons = output.maxVarLen/ChannelReader.PACKED_INT_SIZE;
							initProcessing(totalWidth, totalRows, maxLocatons);
						}
						
						
						//clear histogram totals
						Arrays.fill(workspace, 0);
						if ((activeRow>0) && (activeRow!=totalRows) ) {
							
							logger.error("Image was to have {} rows but only sent {}, producer of images must be fixed", activeRow, totalRows);
							
						}
						activeRow = 0;
						Pipe.confirmLowLevelRead(imgInput, Pipe.sizeOf(imgInput, msgIdx));
						Pipe.releaseReadLock(imgInput);
						
					} else {
						if (-1 != msgIdx) {
							throw new UnsupportedOperationException("Unexpected message idx of:"+msgIdx);
						}
						
						isShuttingDown = true;
						Pipe.confirmLowLevelRead(imgInput, Pipe.EOF_SIZE);
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
			switch (msgIdx) {
				case LocationModeSchema.MSG_CYCLELEARNINGSTART_1:
					activeLearningLocationBase = Pipe.takeInt(pipe);
					learningMaxSlices = Pipe.takeInt(pipe);					
					isLearning = true;
					cycleStep = 0;
					break;
				case LocationModeSchema.MSG_CYCLELEARNINGCANCEL_3:
					
					isLearning = false;
					int j = cycleStep;
					while (--j>=0) {
						locations.removeFromAll(activeLearningLocationBase+j);
					}
					break;
			}
			Pipe.confirmLowLevelRead(pipe, Pipe.sizeOf(pipe, msgIdx));
			Pipe.releaseReadLock(pipe);								
			
		}				
		
	}

	private void readAckData(Pipe<CalibrationStatusSchema> pipe) {
		
		while (Pipe.hasContentToRead(pipe)) {
			
			int msgIdx = Pipe.takeMsgIdx(pipe);
			switch (msgIdx) {

				case CalibrationStatusSchema.MSG_CYCLECALIBRATED_1:
					
					isLearning = false;
					
					final int locationBase = Pipe.takeInt(pipe);
					assert(activeLearningLocationBase == locationBase) : "Completed message did not match location for learning start"; 
					
					final int totalSteps = Pipe.takeInt(pipe);
					
					//un-learn the steps after the point where all agreed.
					int i = cycleStep;
					while (--i>=totalSteps) {
						locations.removeFromAll(locationBase+i);
					}
					
					break;

			}
			Pipe.confirmLowLevelRead(pipe, Pipe.sizeOf(pipe, msgIdx));
			Pipe.releaseReadLock(pipe);								
			
		}				
		
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
			int activeLearningLocationBase) {
		boolean isLoopCompleted = false;
		
		 //the location is a small value so we have come back around
		int endValue = activeLearningLocationBase+learningMaxSlices;
				//minCycles;
		
		//logger.info("checking for cycle complete looking between {} and {}", activeLearningLocationBase, endValue);
		
		int totalMatches = 0;
		int countLimit = (totalWidth*3)/4;
		//logger.info("looking for {} matches in this row of {}", countLimit, totalWidth );
		for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {								
			int readByte = (0xFF&rowData.readByte()>>shiftColors);
			int locationSetId = getLocationSetId(rowBase, activeColumn, readByte);
			if (NO_DATA != locationSetId) {
				
				if (locationSetId<0) {
					//we have just 1 value so we check it
					int value = (SINGLE_BASE-locationSetId);
					//logger.info("looking at single value {}", value);
										
					if ((value>=activeLearningLocationBase) && (value<endValue)) {
									   
							if (isLoopCompleted = (++totalMatches>countLimit)) {
								break;
							}
						
					}
				} else {
					
					//logger.info("looking into range in a set");
					if (locations.containsAny(locationSetId, activeLearningLocationBase, endValue)) {
						
						if (isLoopCompleted = (++totalMatches>countLimit)) {
							break;
						}
						
					}
				}
				
			}// else {
				//logger.info("no locations have been trained at this position");
			//}
				
		}
				
		logger.info("found only {} total matches of {} but must have {} for {} ", totalMatches, totalWidth, countLimit, toString());
		
		return isLoopCompleted;
	}

	private int getLocationSetId(int rowBase, int activeColumn, int readByte) {
		assert(rowBase>=0);
		assert(activeColumn>=0);
		assert(localDepth>=0);
		return imageLookup[
		                               rowBase                            
		                               +(activeColumn*localDepth)
		                               +readByte];
	}

	private void setLocationSetId(int rowBase, int activeColumn, int newId, int readByte) {
		assert(rowBase>=0);
		assert(activeColumn>=0);
		assert(localDepth>=0);
		imageLookup[  rowBase                            
		              +(activeColumn*localDepth)
		              +readByte] = newId;
	}
	

	public boolean save(Pipe<RawDataSchema> pipe) {
		assert (pipe.maxVarLen<(ChannelReader.PACKED_INT_SIZE*4)) : "Pipes must hold longer messages to write this content";
				
		while (Pipe.hasRoomForWrite(pipe)) {					
			int size = Pipe.addMsgIdx(pipe, RawDataSchema.MSG_CHUNKEDSTREAM_1);
			ChannelWriter writer = Pipe.openOutputStream(pipe);
			if (savePosition==NO_DATA) { //new file
				
				writer.writePackedInt(imageWidth);
				writer.writePackedInt(imageHeight);
				writer.writePackedInt(shiftColors);
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
					loadPosition = NO_DATA;//done					
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
			
			if (loadPosition == NO_DATA) {
				
				//note this value here forces us to keep init at 16 and min block at 4
				if (reader.available() < (ChannelReader.PACKED_INT_SIZE*4)) {
					return false;//not enough data yet to read header cleanly
				}
			
				//load all the fixed constants here
				int width  	  = reader.readPackedInt();
				int height 	  = reader.readPackedInt();
				
				shiftColors = reader.readPackedInt();
				localDepth = 256>>shiftColors;
	
				int locations = reader.readPackedInt();//max location value+1
				
				initProcessing(width, height, locations);
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
					loadPosition = NO_DATA;//done
					hasDataSet = true;
				}
				return result;
			}
		}
		return false;
	}

	private void initProcessing(int width, int height, int locations) {
		if (null == workspace || workspace.length != locations) {
			workspace = new int[locations];
		}
		imageWidth = width;
		imageHeight = height;
		final int imageLookupLength = width*height*localDepth;
		
		//init the image matrix as needed		
		if (null ==	imageLookup || imageLookup.length != imageLookupLength) {
			imageLookup = new int[imageLookupLength];
			Arrays.fill(imageLookup, NO_DATA);//this is a marker for no data
		}
		loadPosition = 0;
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
		Pipe.publishWrites(output);
	}

	private void finishedImageProcessing() {
		totalRows = 0;//clear we have sent the value.
		imageInProgress = false;
	}
	

}
