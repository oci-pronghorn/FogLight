package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.image.schema.CalibrationStatusSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class CalibrationCyclicBarierStage extends PronghornStage {

	private Pipe<CalibrationStatusSchema> calibrationDone; 
	private Pipe<CalibrationStatusSchema>[] calibrationDoneInputs;
	
	private int activeLocation = -1;
	private int previousLocation = -1;
	private int activeCount = 0; //lowest common value across the inputs
	
	
	public static CalibrationCyclicBarierStage newInstance(GraphManager gm, 
			Pipe<CalibrationStatusSchema> calibrationDone, 
			Pipe<CalibrationStatusSchema> ... calibrationDoneInputs) {
		return new CalibrationCyclicBarierStage(gm, calibrationDone, calibrationDoneInputs);
	}
	
	public CalibrationCyclicBarierStage(GraphManager gm, 
				Pipe<CalibrationStatusSchema> calibrationDone, 
				Pipe<CalibrationStatusSchema> ... calibrationDoneInputs) {
		super(gm, calibrationDoneInputs, calibrationDone);
		
		this.calibrationDone = calibrationDone;
		this.calibrationDoneInputs = calibrationDoneInputs;
		
	}

	@Override
	public void run() {
		
		//iterate over all 4, if one is smaller pull it and hold value
		//must find lowest number on which they all agree, keep low number
		//bump it up as we see more. once we have all 4 agree, write result and dump all matching base...		
		int i = calibrationDoneInputs.length;
		int sum = 0;
		while (--i>=0) {
			sum += process(calibrationDoneInputs[i]);
		}
		if ((4 == sum) && Pipe.hasRoomForWrite(calibrationDone)) {
			//send selection
			int size = Pipe.addMsgIdx(calibrationDone, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1);			
			Pipe.addIntValue(activeLocation, calibrationDone);
			Pipe.addIntValue(activeCount, calibrationDone);			
			Pipe.confirmLowLevelWrite(calibrationDone, size);
			Pipe.publishWrites(calibrationDone);
			
			//move forward
			previousLocation = activeLocation;
			activeLocation = -1;
			activeCount = 0;
			
			//clear the pipes
			clearOld();
			
		}
		
	}

	private void clearOld() {
		int i = calibrationDoneInputs.length;
		while (--i>=0) {
			while (
					Pipe.peekMsg(calibrationDoneInputs[i], CalibrationStatusSchema.MSG_CYCLECALIBRATED_1)
					&& (Pipe.peekInt(calibrationDoneInputs[i], CalibrationStatusSchema.MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12)==previousLocation)
				  ) {
				Pipe.skipNextFragment(calibrationDoneInputs[i]);					
			}				
		}
	}

	private int process(Pipe<CalibrationStatusSchema> pipe) {
		if (Pipe.peekMsg(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1)) {
			
			int base = Pipe.peekInt(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12);
			if (activeLocation<0) {
				activeLocation = base;
			}
			if (base == activeLocation) {
				int units = Pipe.peekInt(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1_FIELD_TOTALUNITS_13);
				if (units>activeCount) {
					activeCount = units;
				} else if (units<activeCount) {
					do {
						//consume message because some other pipe does not have anything this low.
						Pipe.skipNextFragment(pipe);
					} while ( 							
							 Pipe.peekMsg(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1)							
							 && (activeLocation == Pipe.peekInt(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1_FIELD_STARTVALUE_12))
							 && ((units = Pipe.peekInt(pipe, CalibrationStatusSchema.MSG_CYCLECALIBRATED_1_FIELD_TOTALUNITS_13)) < activeCount) );
					if (units == activeCount) {
						return 1;
					}					
				} else {
					//they equal, we may have something...
					return 1;					
				}
			} else {
				if (base == previousLocation) {
					Pipe.skipNextFragment(pipe);//old from previous check, just remove
				} else {
				    throw new UnsupportedOperationException("Can only resolve 1 location at a time.");
				}
			}
		} else {
			if (Pipe.hasContentToRead(pipe)) {
				Pipe.skipNextFragment(pipe);
				requestShutdown();
			}
		}
		return 0;
	}

}
