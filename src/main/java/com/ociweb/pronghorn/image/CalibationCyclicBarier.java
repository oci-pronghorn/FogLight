package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class CalibationCyclicBarier extends PronghornStage {

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
