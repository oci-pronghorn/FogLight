package com.ociweb.pronghorn.image;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.math.HistogramSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class MapImageStage extends PronghornStage {

	private int[]   workspace;
	private int[][] rowLookup;// [row][(col*256)+depth] full row stored together.
	private int[]   locations;//length + location data, each -1 terminated, no 0 offsets. 	
		
	private final Pipe<ImageSchema> imgInput; 
    private final Pipe<RawDataSchema> loadingMappingData;
    private final Pipe<RawDataSchema> savingMappingData; 
    
	private final Pipe<HistogramSchema> output;
	private boolean isShuttingDown = false;
	private static final Logger logger = LoggerFactory.getLogger(MapImageStage.class);
    	
	public static MapImageStage newInstance(GraphManager graphManager, 
            Pipe<ImageSchema> monochromeInput, 
            Pipe<?> stateData,
            Pipe<HistogramSchema> output,
            Pipe<?> done,
            Pipe<RawDataSchema> loadingMappingData,
            Pipe<RawDataSchema> savingMappingData            
            ) {
		return new MapImageStage(graphManager, monochromeInput, output, loadingMappingData, savingMappingData);
	}
	
	//need outgoing schema for the map.
	protected MapImageStage(GraphManager graphManager, 
			                Pipe<ImageSchema> imgInput, 
			                Pipe<HistogramSchema> output,
			                Pipe<RawDataSchema> loadingMappingData,
			                Pipe<RawDataSchema> savingMappingData 
			               ) {
		
		super(graphManager, join(imgInput,loadingMappingData), join(output,savingMappingData) );
		
		this.imgInput = imgInput;
		this.loadingMappingData = loadingMappingData;
		this.savingMappingData = savingMappingData;
		
		this.output = output;
	}

	private boolean loadingNewMap = false;	
	private boolean imageInProgress = false;
	private int totalRows;
	private int totalWidth;
	private long time;
	private int activeRow;
	
	
	@Override
	public void run() {
		
		if (!isShuttingDown) {
			if (!imageInProgress) {
				loadNewMaps();
			} else {
				//we are image in progress
				if (activeRow == totalRows) {
					//we have a complete message to send
					publishHistogram();
				}				
			}
			
			//if we are not loading a new map check for an image to process
			if (!loadingNewMap) {
				
				while (Pipe.hasContentToRead(imgInput)) {
					
					int msgIdx = Pipe.takeMsgIdx(imgInput);
					
					if (ImageSchema.MSG_FRAMECHUNK_2 == msgIdx) {
						
						if (activeRow < totalRows) {
							
							DataInputBlobReader<ImageSchema> rowData = Pipe.openInputStream(imgInput);
							
							for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {
								int color = rowData.readByte();
								
								int pos = rowLookup[activeRow][(activeColumn*256)+color];
								if (pos >= 0) {
									int idx;
									while ((idx = locations[pos++])!=-1) {
											workspace[idx]++;
									}
								}
							}
							activeRow++;
							if (activeRow == totalRows) {
								publishHistogram();
								break;
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
						Arrays.fill(workspace, 0);
						activeRow = 0;
					} else {
						//shutdown...
						isShuttingDown = true;
						Pipe.confirmLowLevelWrite(imgInput, Pipe.EOF_SIZE);
						Pipe.releaseReadLock(imgInput);
						break;
					}
				}
			}		
		} else {
			if (Pipe.hasRoomForWrite(output)) {
				Pipe.publishEOF(output);
				requestShutdown();
			}
		}
	}

	private void publishHistogram() {
		if (Pipe.hasRoomForWrite(output)) {
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
			
			totalRows = 0;//clear we have sent the value.
			imageInProgress = false;
		}
	}
	
	private void loadNewMaps() {
		 Pipe<RawDataSchema> pipe = loadingMappingData;
		 
		if (Pipe.hasContentToRead(pipe)) {
			loadingNewMap = true;
		}
		
		while (Pipe.hasContentToRead(pipe)) {
		
			int msgIdx = Pipe.takeMsgIdx(pipe);		
			if (msgIdx == RawDataSchema.MSG_CHUNKEDSTREAM_1) {
				DataInputBlobReader.accumLowLevelAPIField(Pipe.inputStream(pipe));
				Pipe.confirmLowLevelRead(pipe, Pipe.sizeOf(pipe, RawDataSchema.MSG_CHUNKEDSTREAM_1));
				Pipe.readNextWithoutReleasingReadLock(pipe);
					
				DataInputBlobReader<RawDataSchema> inputStream = Pipe.inputStream(pipe);
				
				//consume data if possible, note full pipe of data must be large enough for a block.
				if (inputStream.available()>=8) {
					int tempPos = inputStream.absolutePosition();//in case we need to roll back
					int blockType = inputStream.readInt();//4 bytes
					int blockSize = inputStream.readInt();//4 bytes
					if (inputStream.available()>=blockSize) {
						//consume we have all the data for this block
					    if (blockType>=0) {
					    	//row of data
					    	loadRowData(inputStream, blockType);					    	
					    } else {
					    	switch (blockType) {
					    		case -1:
					    			//location data
					    			loadLocationData(inputStream);					    			
					    			break;
					    		case -2:
					    			//meta data
					    			initMapData(inputStream); //this message will be first
						    		break;
					    		case -3:
					    			//end of file  //this message will be last
					    			finishMapData(inputStream);					    			
						    		break;						    			
					    	}
					    }
					} else {
						//roll back and try again later
						inputStream.absolutePosition(tempPos);
					}
				}
			} else {
				assert(-1 == msgIdx); //shutdown logic				
				Pipe.skipNextFragment(pipe, msgIdx);
				//shutdown from file system is not recognized as a shutdown of the system.
				assert(Pipe.inputStream(pipe).available()==0);
				loadingNewMap = false;
			}
		}
	}

	private void initMapData(DataInputBlobReader<RawDataSchema> inputStream) {
		//load all the fixed constants here
		int width  	  = inputStream.readPackedInt();
		int height 	  = inputStream.readPackedInt();
		int depth  	  = inputStream.readPackedInt();
		int locations = inputStream.readPackedInt();//max location value+1
		
		if (null == workspace || workspace.length != locations) {
			workspace = new int[locations];
		}
		
		//init the image matrix as needed		
		if (null ==	rowLookup || rowLookup.length != height) {
			rowLookup = new int[height][];
		}
		
		int rowLen = width*depth;
		int i = height;
		while (--i>=0) {
			if (null == rowLookup[i] ||  rowLookup[i].length != rowLen) {
				rowLookup[i] = new int[rowLen];
			} else {
				Arrays.fill(rowLookup[i], 0);
			}
		}
		
	}
	
	private void finishMapData(DataInputBlobReader<RawDataSchema> inputStream) {
		assert(inputStream.available()==0);
		loadingNewMap = false;
	}


	private void loadLocationData(DataInputBlobReader<RawDataSchema> inputStream) {
		int arrayLength = inputStream.readPackedInt();
		if (null==locations || arrayLength != locations.length) {
			locations = new int[arrayLength];
		}
		for(int i = 0; i<arrayLength; i++) {
			locations[i] = inputStream.readPackedInt();
		}
	}

	private void loadRowData(DataInputBlobReader<RawDataSchema> inputStream, int row) {
		int[] target = rowLookup[row];
		int len = target.length;
		for(int i = 0; i<len; i++) {
			target[i] = inputStream.readPackedInt();
		}
	}

}
