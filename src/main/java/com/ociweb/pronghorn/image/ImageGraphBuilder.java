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
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

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

		Pipe<LocationModeSchema>   modePipe   = LocationModeSchema.instance.newPipe(6, 100); 
		Pipe<CalibrationStatusSchema> calibrationDone = CalibrationStatusSchema.instance.newPipe(6, 0);
	    Pipe<LocationModeSchema> imageStateDataR = LocationModeSchema.instance.newPipe(6, 100); 
	    Pipe<LocationModeSchema> imageStateDataG = LocationModeSchema.instance.newPipe(6, 100); 
	    Pipe<LocationModeSchema> imageStateDataB = LocationModeSchema.instance.newPipe(6, 100); 
	    Pipe<LocationModeSchema> imageStateDataM = LocationModeSchema.instance.newPipe(6, 100);
		
	    
	    Pipe<CalibrationStatusSchema> calibrationDoneR = CalibrationStatusSchema.instance.newPipe(6, 0);
	    Pipe<CalibrationStatusSchema> calibrationDoneG = CalibrationStatusSchema.instance.newPipe(6, 0); 
	    Pipe<CalibrationStatusSchema> calibrationDoneB = CalibrationStatusSchema.instance.newPipe(6, 0);
	    Pipe<CalibrationStatusSchema> calibrationDoneM = CalibrationStatusSchema.instance.newPipe(6, 0); 
	    
	    ModeManageState.newInstance(gm, modePipe, calibrationDone,
	    		            imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM );
	    
	    CalibationCyclicBarier.newInstance(gm, 
	    					calibrationDone,
	    		            calibrationDoneR, calibrationDoneG, calibrationDoneB, calibrationDoneM);
	    
	    
	    
		//need ModeStateIn, CalibrationDoneOut
		MapImageStage.newInstance(gm, imageR, imageStateDataR, histR, calibrationDoneR, loadDataRed,   saveDataRed);
		MapImageStage.newInstance(gm, imageG, imageStateDataG, histG, calibrationDoneG, loadDataGreen, saveDataGreen);
		MapImageStage.newInstance(gm, imageB, imageStateDataB, histB, calibrationDoneB, loadDataBlue,  saveDataBlue);
		MapImageStage.newInstance(gm, imageM, imageStateDataM, histM, calibrationDoneM, loadDataMono,  saveDataMono);
		
		
		HistogramSumStage.newInstance(gm, histSum, histR, histG, histB, histM);
		HistogramSelectPeakStage.newInstance(gm, histSum, probLocation );

		RawDataJoiner.newInstance(gm, 
				          saveWrite, saveAck, 
				          saveDataRed, saveDataGreen, saveDataBlue, saveDataMono);
		
		String targetFilePath = "";//only used for save on shutdown logic
		BlockStorageStage.newInstance(gm, targetFilePath, saveWrite, saveAck);
		
		FileBlobWriteStage.newInstance(gm, saveDataRaw, targetFilePath);
		
		//TODO: this should not be in this method instead the probLocation pipe is returned or passed in
		//      the external caller will take this pipe and connect it to the reactive listener...
		ConsoleJSONDumpStage.newInstance(gm, probLocation); //TODO: feed this back to caller, react..
		
		
		//TODO: modePipe must be written to by some external stage to begin learning mode..
		
	}
	
	
}
