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
		
	private final Pipe<ImageSchema> imgInput; 
    private final Pipe<RawDataSchema> loadingMappingData;
    private final Pipe<RawDataSchema> savingMappingData; 
    
	private final Pipe<HistogramSchema> output;
	private boolean isShuttingDown = false;
	
	private boolean loadingNewMap = false;	
	private boolean imageInProgress = false;
	private int totalRows;
	private int totalWidth;
	private long time;
	private int activeRow;
	private LoisVisitor sumVisitor = new LoisVisitor() {

	//TODO: ad traning of loop
    //TODO: must pass in uppper range so we can detect this when encountered.
		
		
		
		@Override
		public boolean visit(int value) {
			workspace[value]++;
			return true;
		}
		
	};
	
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
							
							int rowBase = activeRow*(imageWidth*256);
							for(int activeColumn = 0; activeColumn<totalWidth; activeColumn++) {
								locations.visitSet(imageLookup[
								                               rowBase                            
								                               +(activeColumn*256)
								                               +(int)rowData.readByte()], sumVisitor );
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
			if (savePosition==-3 || save(savingMappingData)) {
				if (Pipe.hasRoomForWrite(output)) {
					Pipe.publishEOF(output);
					requestShutdown();
				}
			}
		}
	}

	private transient int loadPosition = -1;
	private transient int savePosition = -1;
	
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
	

}
