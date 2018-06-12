package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.file.FileBlobReadStage;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class ImageGraphBuilder {

	
	public static void buildLocationDetectionGraph(
								GraphManager gm, String dataFilePath
								
				) {
				
		Pipe<RawDataSchema> mData  = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataR = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataG = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataB = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> mDataM = RawDataSchema.instance.newPipe(8, 1<<12);
		
		Pipe<RawDataSchema> mDataRRead = new Pipe(mDataR.config().grow2x());
		Pipe<RawDataSchema> mDataGRead = new Pipe(mDataG.config().grow2x());
		Pipe<RawDataSchema> mDataBRead = new Pipe(mDataB.config().grow2x());
		Pipe<RawDataSchema> mDataMRead = new Pipe(mDataM.config().grow2x());
		
		Pipe<RawDataSchema> mDataRWrite = new Pipe(mDataR.config().grow2x());
		Pipe<RawDataSchema> mDataGWrite = new Pipe(mDataG.config().grow2x());
		Pipe<RawDataSchema> mDataBWrite = new Pipe(mDataB.config().grow2x());
		Pipe<RawDataSchema> mDataMWrite = new Pipe(mDataM.config().grow2x());
		
		
		FileBlobReadStage readStage = FileBlobReadStage.newInstance(gm, mData, dataFilePath);		
		RawDataSplitter.newInstance(gm, gm.getOutputPipe(gm, readStage),
				                    mDataR, mDataG, mDataB, mDataM);
		
		ReplicatorStage.newInstance(gm, mDataR, mDataRRead, mDataRWrite);
		ReplicatorStage.newInstance(gm, mDataG, mDataGRead, mDataGWrite);
		ReplicatorStage.newInstance(gm, mDataB, mDataBRead, mDataBWrite);
		ReplicatorStage.newInstance(gm, mDataM, mDataMRead, mDataMWrite);
		
		
		
		
		//these added just as place holders for now.
		
		PipeCleanerStage.newInstance(gm, mDataRRead); //these go to the image mappping to location logic
		PipeCleanerStage.newInstance(gm, mDataGRead);
		PipeCleanerStage.newInstance(gm, mDataBRead);
		PipeCleanerStage.newInstance(gm, mDataMRead);
		
		
		PipeCleanerStage.newInstance(gm, mDataRWrite); //these go to the image learning mapping to location
		PipeCleanerStage.newInstance(gm, mDataGWrite);
		PipeCleanerStage.newInstance(gm, mDataBWrite);
		PipeCleanerStage.newInstance(gm, mDataMWrite);
		
		
		
	}
	
	
}
