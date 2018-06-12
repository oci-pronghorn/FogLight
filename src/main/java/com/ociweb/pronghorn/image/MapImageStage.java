package com.ociweb.pronghorn.image;

import java.util.Arrays;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.math.HistogramSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class MapImageStage extends PronghornStage {

	//disk structure
	
	//de res width  (int)
	//de res height (int)
	//de depth      (int) //256 default
	
	//Each of the <height> rows of data with depth data, length is width*depth
	//<length of location data> followed by Single location data array

	private int[]   workspace;

	private int[][] rowLookup;// [row][(col*256)+depth] full row stored together.
	private int[]   locations;//length + location data, each -1 terminated, no 0 offsets. 	
		
	private final Pipe<ImageSchema> monochromeInput; 
	private final Pipe<RawDataSchema>[] newMappingData;
	private final Pipe<HistogramSchema> output;

    	
	//need outgoing schema for the map.
	protected MapImageStage(GraphManager graphManager, 
			                Pipe<ImageSchema> monochromeInput, 
			                Pipe<RawDataSchema>[] newMappingData,
			                Pipe<HistogramSchema> output) {
		
		super(graphManager, monochromeInput, output);
		
		this.monochromeInput = monochromeInput;
		this.newMappingData = newMappingData;
		this.output = output;
	}

	private int loadingNewMap = -1;	
	private boolean imageInProgress = false;
	
	@Override
	public void run() {
		
		if (!imageInProgress) {
			if (loadingNewMap >= 0) {
				loadNewMaps(loadingNewMap);
			} else {
				int n = newMappingData.length;
				while (--n>=0) {
					loadNewMaps(n);
				}
			}
		}
		
		//if we are not loading a new map check for an image to process
		if (loadingNewMap<0) {
			
			
			//read in 1 image and based on the locations look up the summary for the map
			
			//		int row = 0;
			//		int column = 0;
			//		int brightness = 0;
			//		
			//		int pos = map[row][column][brightness];
			//		int idx;
			//		while ((idx = locations[pos++])!=-1) {
			//				workspace[idx]++;
			//		}
						
			
		}		
	}
	
	private void loadNewMaps(int n) {
		 Pipe<RawDataSchema> pipe = newMappingData[n];
		if (Pipe.hasContentToRead(pipe)) {
			loadingNewMap = n;
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
				loadingNewMap = -1;
			}
		}
	}

	private void initMapData(DataInputBlobReader<RawDataSchema> inputStream) {
		//load all the fixed constants here
		int width  	= inputStream.readPackedInt();
		int height 	= inputStream.readPackedInt();
		int depth  	= inputStream.readPackedInt();
		
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
		loadingNewMap = -1;
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
