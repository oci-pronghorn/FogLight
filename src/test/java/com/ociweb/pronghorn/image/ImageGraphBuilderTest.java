package com.ociweb.pronghorn.image;

import org.junit.Test;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.image.schema.CalibrationStatusSchema;
import com.ociweb.pronghorn.image.schema.ImageSchema;
import com.ociweb.pronghorn.image.schema.LocationModeSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.math.ProbabilitySchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;
import com.ociweb.pronghorn.stage.test.PipeNoOp;

public class ImageGraphBuilderTest {


	@Test
	public void buildGraphTest() {
		
		
		//ScriptedNonThreadScheduler.debugStageOrder = System.out;
		//ScriptedNonThreadScheduler.globalStartupLockCheck = true;
		
		GraphManager gm = new GraphManager();
		
		String loadFilePath = null;
		String saveFilePath = HardwareImpl.generateFilePath("savedLocation",".dat");
		
		Pipe<ImageSchema>             imagePipe = ImageSchema.instance.newPipe(2000, 4000);
		imagePipe.initBuffers();
		
		Pipe<LocationModeSchema>      modeSelectionPipe = LocationModeSchema.instance.newPipe(10, 0) ;
		modeSelectionPipe.initBuffers();
		
		LocationModeSchema.publishCycleLearningStart(modeSelectionPipe, 10000, 1000);
		
		
		
		final int fieldWidth = 6400;
		final int fieldHeight = 3600;
		long fieldTimestamp = System.currentTimeMillis();
		int fieldFrameBytes =  30000;
		int fieldBitsPerPixel = 24;
		byte[] fieldEncodingBacking = "RGB24".getBytes();
		int fieldEncodingPosition = 0;
		int fieldEncodingLength = fieldEncodingBacking.length;
		
		ImageSchema.publishFrameStart(imagePipe, fieldWidth, fieldHeight, fieldTimestamp, 
				                      fieldFrameBytes, fieldBitsPerPixel, fieldEncodingBacking, fieldEncodingPosition, fieldEncodingLength);
		
		PipeNoOp.newInstance(gm, imagePipe);
		PipeNoOp.newInstance(gm, modeSelectionPipe);
		
		int i = 1000;
		while (--i>=0) {
			
			byte[] fieldRowBytesBacking= new byte[3*fieldWidth];
			int fieldRowBytesPosition = 0;
			int fieldRowBytesLength = fieldRowBytesBacking.length;
			
			ImageSchema.publishFrameChunk(imagePipe, fieldRowBytesBacking, fieldRowBytesPosition, fieldRowBytesLength);
			
		}
		
		
		Pipe<ProbabilitySchema>       probLocation = ProbabilitySchema.instance.newPipe(100, 2000);
		Pipe<CalibrationStatusSchema> calibrationDone = CalibrationStatusSchema.instance.newPipe(10, 0);

		PipeCleanerStage.newInstance(gm, probLocation);
		PipeCleanerStage.newInstance(gm, calibrationDone);
		
		ImageGraphBuilder.buildLocationDetectionGraph(gm, 
													loadFilePath, saveFilePath, imagePipe, 
													modeSelectionPipe, probLocation, calibrationDone);
		
		gm.enableTelemetry(8098);
		StageScheduler scheduler = StageScheduler.defaultScheduler(gm);
		
		
		scheduler.startup();
		
		
		
		try {
			
			Thread.sleep(1_000);			
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
		
		scheduler.shutdown();
		
		
		
	}
	
}
