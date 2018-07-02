package com.ociweb.pronghorn.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ociweb.pronghorn.image.schema.CalibrationStatusSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class CalibrationCyclicBarierStageTest {

	@Test
	public void simpleTest() {
		
		
		GraphManager gm = new GraphManager();
		
		int tracks = 4;
		
		Pipe<CalibrationStatusSchema> calibrationDoneOutput = CalibrationStatusSchema.instance.newPipe(10, 100);
		Pipe<CalibrationStatusSchema>[] calibrationDoneInputs = Pipe.buildPipes(tracks, calibrationDoneOutput.config());
		
		int i = tracks;
		while (--i>=0) {
			calibrationDoneInputs[i].initBuffers();
			
			int fieldStartValue = 10000;
			int fieldTotalUnits =  1000;
			CalibrationStatusSchema.publishCycleCalibrated(calibrationDoneInputs[i], fieldStartValue, fieldTotalUnits);
			
			Pipe.publishEOF(calibrationDoneInputs[i]);
		}
		
		
		new CalibrationCyclicBarierStage(gm, calibrationDoneOutput, calibrationDoneInputs);
		
		StringBuilder results = new StringBuilder();
		PronghornStage watch = ConsoleJSONDumpStage.newInstance(gm, calibrationDoneOutput, results);
		
		StageScheduler sched = StageScheduler.defaultScheduler(gm);
		
		sched.startup();
		
		GraphManager.blockUntilStageTerminated(gm, watch);

		assertEquals("{\"CycleCalibrated\":  {\"StartValue\":10000}  {\"TotalUnits\":1000}}\n",results.toString());
		
	}
	
	
}
