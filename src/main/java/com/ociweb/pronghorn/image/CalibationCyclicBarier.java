package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class CalibationCyclicBarier extends PronghornStage {

	public static CalibationCyclicBarier newInstance(GraphManager gm, 
			Pipe<?> calibrationDone, 
			Pipe<?> calibrationDoneR, Pipe<?> calibrationDoneG, Pipe<?> calibrationDoneB, Pipe<?> calibrationDoneM) {
		return new CalibationCyclicBarier(gm, calibrationDone, 
				       calibrationDoneR, calibrationDoneG, calibrationDoneB, calibrationDoneM);
	}
	
	public CalibationCyclicBarier(GraphManager gm, 
				Pipe<?> calibrationDone, 
				Pipe<?> calibrationDoneR, Pipe<?> calibrationDoneG, Pipe<?> calibrationDoneB, Pipe<?> calibrationDoneM) {
		super(gm, join(calibrationDoneR, calibrationDoneG, calibrationDoneB, calibrationDoneM), calibrationDone);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
