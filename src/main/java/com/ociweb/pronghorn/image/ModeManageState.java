package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ModeManageState extends PronghornStage {

	
	public static ModeManageState newInstance(GraphManager gm,
            Pipe<LocationModeSchema> modeIn, Pipe<CalibrationStatusSchema> calibrationDone, 
            Pipe<LocationModeSchema> imageStateDataR,	Pipe<LocationModeSchema> imageStateDataG, Pipe<LocationModeSchema> imageStateDataB, Pipe<LocationModeSchema> imageStateDataM) {
		return new ModeManageState(gm, modeIn, calibrationDone, imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM);
	}

	
	public ModeManageState(GraphManager gm,
			               Pipe<LocationModeSchema> modeIn, Pipe<CalibrationStatusSchema> calibrationDone, 
			               Pipe<LocationModeSchema> imageStateDataR,	Pipe<LocationModeSchema> imageStateDataG, Pipe<LocationModeSchema> imageStateDataB, Pipe<LocationModeSchema> imageStateDataM) {
		super(gm, join(modeIn,calibrationDone), join(imageStateDataR, imageStateDataG, imageStateDataB, imageStateDataM) );
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
