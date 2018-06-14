package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ModeManageState extends PronghornStage {

	
	public static ModeManageState newInstance(GraphManager gm,
            Pipe<?> tickTrigger, Pipe<?> calibrationDone, 
            Pipe<?> imageStateDataR,	Pipe<?> imageStateDataG, Pipe<?> imageStateDataB, Pipe<?> imageStateDataM) {
		return new ModeManageState(gm, tickTrigger, calibrationDone, imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM);
	}

	
	public ModeManageState(GraphManager gm,
			               Pipe<?> tickTrigger, Pipe<?> calibrationDone, 
			               Pipe<?> imageStateDataR,	Pipe<?> imageStateDataG, Pipe<?> imageStateDataB, Pipe<?> imageStateDataM) {
		super(gm, join(tickTrigger,calibrationDone), join(imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM) );
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
