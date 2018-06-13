package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.iot.schema.ImageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.file.BlockStorageStage;
import com.ociweb.pronghorn.stage.file.FileBlobReadStage;
import com.ociweb.pronghorn.stage.file.FileBlobWriteStage;
import com.ociweb.pronghorn.stage.file.schema.BlockStorageReceiveSchema;
import com.ociweb.pronghorn.stage.file.schema.BlockStorageXmitSchema;
import com.ociweb.pronghorn.stage.math.HistogramSchema;
import com.ociweb.pronghorn.stage.math.HistogramSelectPeakStage;
import com.ociweb.pronghorn.stage.math.HistogramSumStage;
import com.ociweb.pronghorn.stage.math.ProbabilitySchema;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;
import com.ociweb.pronghorn.stage.test.PipeNoOp;

public class ImageGraphBuilder {

	
	public static void buildLocationDetectionGraph(
								GraphManager gm, String dataFilePath
								
				) {
				
		//////////////////////////////////
		//Pipe definitions
		/////////////////////////////////
		
		Pipe<RawDataSchema> loadDataRaw   = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> loadDataRed   = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> loadDataGreen = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> loadDataBlue  = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> loadDataMono  = RawDataSchema.instance.newPipe(8, 1<<12);
		
	
		Pipe<RawDataSchema> saveDataRaw   = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> saveDataRed   = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> saveDataGreen = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> saveDataBlue  = RawDataSchema.instance.newPipe(8, 1<<12);
		Pipe<RawDataSchema> saveDataMono  = RawDataSchema.instance.newPipe(8, 1<<12);
		
		//TODO: these 4 need to be populated by down res stage
		//TODO: Brandon these following 4 pipes must be populated by the new stage..
		Pipe<ImageSchema> imageR = ImageSchema.instance.newPipe(2048, 8096);
		Pipe<ImageSchema> imageG = ImageSchema.instance.newPipe(2048, 8096);
		Pipe<ImageSchema> imageB = ImageSchema.instance.newPipe(2048, 8096);
		Pipe<ImageSchema> imageM = ImageSchema.instance.newPipe(2048, 8096);

		
		Pipe<HistogramSchema> histR = HistogramSchema.instance.newPipe(4, 1<<12);
		Pipe<HistogramSchema> histG = HistogramSchema.instance.newPipe(4, 1<<12);
		Pipe<HistogramSchema> histB = HistogramSchema.instance.newPipe(4, 1<<12);
		Pipe<HistogramSchema> histM = HistogramSchema.instance.newPipe(4, 1<<12);
		
		Pipe<HistogramSchema> histSum = HistogramSchema.instance.newPipe(4, 1<<12);		
		Pipe<ProbabilitySchema> probLocation = ProbabilitySchema.instance.newPipe(4, 1<<14);
		
		Pipe<BlockStorageXmitSchema> saveWrite = BlockStorageXmitSchema.instance
													.newPipe(8, 1<<12);
		
		Pipe<BlockStorageReceiveSchema> saveAck = BlockStorageReceiveSchema.instance
				                                    .newPipe(8, 1<<12);
		
		////////////////////////////////////////
		//Stage definitions
		////////////////////////////////////////
		
		
		//data is only read once on startup
		FileBlobReadStage readStage = FileBlobReadStage.newInstance(gm, loadDataRaw, dataFilePath);		
		RawDataSplitter.newInstance(gm, 
				                    gm.getOutputPipe(gm, readStage),
				                    loadDataRed, loadDataGreen, loadDataBlue, loadDataMono);

		Pipe<?> tickTrigger     = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
		Pipe<?> calibrationDone = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> imageStateDataR = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> imageStateDataG = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> imageStateDataB = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> imageStateDataM = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
		//TODO: brandon: must feed tickTicker from the image capture stage
	    
	    Pipe<?> calibrationDoneR = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> calibrationDoneG = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> calibrationDoneB = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    Pipe<?> calibrationDoneM = RawDataSchema.instance.newPipe(1, 1); //HACK until we define this schema
	    
	    new ModeManageState(gm, tickTrigger, calibrationDone,
	    		            imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM );
	    
	    new CalibationCyclicBarier(gm, 
	    					calibrationDone,
	    		            calibrationDoneR, calibrationDoneG, calibrationDoneB, calibrationDoneM);
	    
	    
	    
		//need ModeStateIn, CalibrationDoneOut
		MapImageStage.newInstance(gm, imageR, imageStateDataR, histR, calibrationDoneR, loadDataRed,   saveDataRed);
		MapImageStage.newInstance(gm, imageG, imageStateDataG, histG, calibrationDoneG, loadDataGreen, saveDataGreen);
		MapImageStage.newInstance(gm, imageB, imageStateDataB, histB, calibrationDoneB, loadDataBlue,  saveDataBlue);
		MapImageStage.newInstance(gm, imageM, imageStateDataM, histM, calibrationDoneM, loadDataMono,  saveDataMono);
		
		
		HistogramSumStage.newInstance(gm, histSum, histR, histG, histB, histM);
		HistogramSelectPeakStage.newInstance(gm, histSum, probLocation );

		new RawDataJoiner(gm, 
				          saveWrite, saveAck, saveDataRed, saveDataGreen, saveDataBlue, saveDataMono);
		
		String targetFilePath = "";//only used for save on shutdown logic
		BlockStorageStage.newInstance(gm, targetFilePath, saveWrite, saveAck);
		
		FileBlobWriteStage.newInstance(gm, saveDataRaw, targetFilePath);
		
		//TODO: this should not be in this method instead the probLocation pipe is returned or passed in
		//      the external caller will take this pipe and connect it to the reactive listener...
		ConsoleJSONDumpStage.newInstance(gm, probLocation); //TODO: feed this back to caller, react..
		
		
	}
	
	
}
