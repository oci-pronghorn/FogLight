package com.ociweb.pronghorn.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ociweb.pronghorn.image.schema.CalibrationStatusSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class CalibrationCyclicBarierStageSecondTest {

	@Test
	public void aTest() {
				
		GraphManager gm = new GraphManager();
		
		int tracks = 4;
		int[][] pub = new int[][] {
				{2,4,5},
				{4,5},
				{5,6,7},
				{4,5,6}
		};
				
		runGraph(gm, tracks, pub, 5);
		
	}
	
	@Test
	public void bTest() {
				
		GraphManager gm = new GraphManager();
		
		int tracks = 4;
		int[][] pub = new int[][] {
				{2,4,5},
				{5},
				{5,6,7},
				{4,5,6}
		};
				
		runGraph(gm, tracks, pub, 5);
		
	}
	
	@Test
	public void cTest() {
				
		GraphManager gm = new GraphManager();
		
		int tracks = 4;
		int[][] pub = new int[][] {
				{5},
				{2,4,5},
				{4,5,6},
				{5,6,7},
		};
				
		runGraph(gm, tracks, pub, 5);
		
	}
	
	

	private void runGraph(GraphManager gm, int tracks, int[][] pub, int expected) {
		Pipe<CalibrationStatusSchema> calibrationDoneOutput = CalibrationStatusSchema.instance.newPipe(30, 0);
		Pipe<CalibrationStatusSchema>[] calibrationDoneInputs = Pipe.buildPipes(tracks, calibrationDoneOutput.config());

		
		new CalibrationCyclicBarierStage(gm, calibrationDoneOutput, calibrationDoneInputs);
		
		StringBuilder results = new StringBuilder();
		PronghornStage watch = ConsoleJSONDumpStage.newInstance(gm, calibrationDoneOutput, results);
		
		StageScheduler sched = StageScheduler.defaultScheduler(gm);
		
		sched.startup();
		
		
		publishData(tracks, pub, calibrationDoneInputs);
		try {
			//add delay to force code to deal with each part
			Thread.sleep(400);
		} catch (InterruptedException e) {
		}
		publishEOF(tracks, calibrationDoneInputs);
		
		
		GraphManager.blockUntilStageTerminated(gm, watch);

		assertEquals("{\"CycleCalibrated\":  {\"StartValue\":10000}  {\"TotalUnits\":"+expected+"}}\n",results.toString());
	}

	private void publishData(int tracks, int[][] pub, Pipe<CalibrationStatusSchema>[] calibrationDoneInputs) {
		int i = tracks;
		while (--i>=0) {
		
			int fieldStartValue = 10000;
			
			for(int j = 0; j<pub[i].length; j++) {			
				CalibrationStatusSchema.publishCycleCalibrated(calibrationDoneInputs[i], fieldStartValue, pub[i][j]);
			}
			try {
				//add delay to force code to deal with each part
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	
	
	private void publishEOF(int tracks, Pipe<CalibrationStatusSchema>[] calibrationDoneInputs) {
		int i = tracks;
		while (--i>=0) {
		
			Pipe.publishEOF(calibrationDoneInputs[i]);
		}
	}
	
}
