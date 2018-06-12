package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.file.FileBlobReadStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ImageGraphBuilder {

	
	public static void buildLocationDetectionGraph(
								GraphManager gm, String dataFilePath
								
				) {
				
		Pipe<RawDataSchema> mData  = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataR = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataG = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataB = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataM = RawDataSchema.instance.newPipe(8, 1<<12);
		
		
		FileBlobReadStage mappingDataFileReader = new FileBlobReadStage(gm, mData, dataFilePath);
		
		new RawDataSplitter(gm, mData, mDataR, mDataG, mDataB, mDataM);
		
		
		
		
	}
	
	
}
